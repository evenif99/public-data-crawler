package com.evenif.crawler.pressrelease.crawler;

import com.evenif.crawler.common.crawl.HtmlFetcher;
import com.evenif.crawler.common.crawl.RobotsTxtRules;
import com.evenif.crawler.pressrelease.PressRelease;
import com.evenif.crawler.pressrelease.PressReleaseRepository;
import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 정책브리핑 보도자료 수집: robots.txt 확인 → 목록 → 상세 → 저장.
 * 스케줄러·실행 이력·재시도는 다음 단계에서 붙인다.
 */
@Service
public class KoreaPolicyCrawlService {

    private static final Logger log = LoggerFactory.getLogger(KoreaPolicyCrawlService.class);
    static final String LIST_PATH = "/briefing/pressReleaseList.do";
    private static final int MAX_PAGES_LIMIT = 10;

    private final HtmlFetcher fetcher;
    private final KoreaPolicyListParser listParser;
    private final KoreaPolicyDetailParser detailParser;
    private final PressReleaseRepository repository;
    private final KoreaPolicyProperties props;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public KoreaPolicyCrawlService(HtmlFetcher fetcher,
                                   KoreaPolicyListParser listParser,
                                   KoreaPolicyDetailParser detailParser,
                                   PressReleaseRepository repository,
                                   KoreaPolicyProperties props) {
        this.fetcher = fetcher;
        this.listParser = listParser;
        this.detailParser = detailParser;
        this.repository = repository;
        this.props = props;
    }

    /** @param maxPages null이면 설정값 사용, 최대 10페이지 */
    public CrawlResult crawl(Integer maxPages) {
        if (!running.compareAndSet(false, true)) {
            throw new CrawlAlreadyRunningException();
        }
        try {
            int pages = Math.min(maxPages != null ? maxPages : props.maxPages(), MAX_PAGES_LIMIT);
            return doCrawl(Math.max(pages, 1));
        } finally {
            running.set(false);
        }
    }

    private CrawlResult doCrawl(int maxPages) {
        LocalDateTime startedAt = LocalDateTime.now();
        Counter c = new Counter();

        RobotsTxtRules robots;
        try {
            robots = loadRobots();
        } catch (IOException e) {
            log.warn("robots.txt를 읽지 못해 수집을 중단합니다: {}", e.toString());
            return c.result(startedAt, "robots.txt 확인 실패: " + e.getMessage());
        }
        if (!robots.isAllowed(LIST_PATH) || !robots.isAllowed(KoreaPolicyListParser.VIEW_PATH)) {
            return c.result(startedAt, "robots.txt에서 수집이 허용되지 않음");
        }

        for (int page = 1; page <= maxPages; page++) {
            String listUrl = props.baseUrl() + LIST_PATH + "?pageIndex=" + page;
            List<PressReleaseListItem> items;
            try {
                Document listDoc = fetcher.fetchDocument(listUrl);
                items = listParser.parse(listDoc, props.baseUrl());
            } catch (IOException e) {
                log.warn("목록 페이지 요청 실패 {}: {}", listUrl, e.toString());
                return c.result(startedAt, "목록 페이지 요청 실패(page=" + page + "): " + e.getMessage());
            }
            c.pages++;
            log.info("목록 {}페이지: {}건", page, items.size());
            if (items.isEmpty()) {
                if (page == 1) {
                    return c.result(startedAt, "목록에서 글을 찾지 못함 (HTML 구조 변경 가능성)");
                }
                break;
            }

            int newOnPage = 0;
            for (PressReleaseListItem item : items) {
                c.found++;
                if (repository.existsByNewsId(item.newsId())) {
                    c.skipped++;
                    continue;
                }
                if (collect(item)) {
                    c.saved++;
                    newOnPage++;
                } else {
                    c.failed++;
                }
            }
            // 한 페이지가 전부 이미 저장된 글이면 그 뒤 페이지도 수집된 것으로 보고 멈춘다
            if (newOnPage == 0 && c.failed == 0) {
                break;
            }
        }
        return c.result(startedAt, null);
    }

    private boolean collect(PressReleaseListItem item) {
        PressReleaseDetail detail;
        try {
            detail = detailParser.parse(fetcher.fetchDocument(item.detailUrl()));
        } catch (IOException e) {
            log.warn("상세 페이지 요청 실패 newsId={}: {}", item.newsId(), e.toString());
            return false;
        }
        if (detail.body() == null) {
            log.warn("본문 영역을 찾지 못함 newsId={} (요약만 저장)", item.newsId());
        }

        PressRelease entity = new PressRelease(
                item.newsId(),
                truncate(firstNonNull(detail.title(), item.title()), 500),
                truncate(firstNonNull(item.ministry(), detail.ministry()), 100),
                firstNonNull(item.publishedDate(), detail.publishedDate()),
                item.summary(),
                detail.body(),
                item.detailUrl(),
                LocalDateTime.now());
        try {
            repository.save(entity);
            return true;
        } catch (DataIntegrityViolationException e) {
            // 동시에 같은 글이 저장된 경우: 유니크 제약이 최종 방어선
            log.info("이미 저장된 글 newsId={}", item.newsId());
            return false;
        }
    }

    private RobotsTxtRules loadRobots() throws IOException {
        try {
            return RobotsTxtRules.parse(fetcher.fetchText(props.baseUrl() + "/robots.txt"));
        } catch (HttpStatusException e) {
            if (e.getStatusCode() >= 400 && e.getStatusCode() < 500) {
                return RobotsTxtRules.allowAll(); // robots.txt가 없으면 제한 없음 (RFC 9309)
            }
            throw e;
        }
    }

    private static <T> T firstNonNull(T a, T b) {
        return a != null ? a : b;
    }

    private static String truncate(String s, int max) {
        return (s == null || s.length() <= max) ? s : s.substring(0, max);
    }

    private static final class Counter {
        int pages;
        int found;
        int saved;
        int skipped;
        int failed;

        CrawlResult result(LocalDateTime startedAt, String message) {
            return new CrawlResult(startedAt, LocalDateTime.now(),
                    pages, found, saved, skipped, failed, message);
        }
    }
}
