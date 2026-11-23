package com.evenif.crawler.pressrelease.crawler;

import com.evenif.crawler.common.crawl.HtmlFetcher;
import com.evenif.crawler.pressrelease.PressRelease;
import com.evenif.crawler.pressrelease.PressReleaseRepository;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** 실제 네트워크 대신 fixture를 돌려주는 fetcher + H2(메모리 DB)로 수집 흐름을 확인한다. */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:crawltest;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "crawler.korea-policy.base-url=" + Fixtures.BASE_URL
})
class KoreaPolicyCrawlServiceTest {

    static final String LIST_1 = Fixtures.BASE_URL + "/briefing/pressReleaseList.do?pageIndex=1";
    static final String LIST_2 = Fixtures.BASE_URL + "/briefing/pressReleaseList.do?pageIndex=2";

    @TestConfiguration
    static class Config {
        @Bean
        @Primary
        FakeFetcher fakeFetcher() {
            return new FakeFetcher();
        }
    }

    static class FakeFetcher implements HtmlFetcher {
        final Map<String, String> pages = new HashMap<>();
        final List<String> requested = new ArrayList<>();

        @Override
        public Document fetchDocument(String url) throws IOException {
            return Jsoup.parse(fetchText(url), url);
        }

        @Override
        public String fetchText(String url) throws IOException {
            requested.add(url);
            String html = pages.get(url);
            if (html == null) {
                throw new HttpStatusException("not found", 404, url);
            }
            return html;
        }
    }

    @Autowired
    KoreaPolicyCrawlService service;
    @Autowired
    PressReleaseRepository repository;
    @Autowired
    FakeFetcher fetcher;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        fetcher.pages.clear();
        fetcher.requested.clear();
        fetcher.pages.put(Fixtures.BASE_URL + "/robots.txt", "User-agent: *\nAllow: /\n");
        fetcher.pages.put(LIST_1, Fixtures.read("list.html"));
        fetcher.pages.put(KoreaPolicyListParser.detailUrl(Fixtures.BASE_URL, "156790270"),
                Fixtures.read("detail.html"));
        fetcher.pages.put(KoreaPolicyListParser.detailUrl(Fixtures.BASE_URL, "156790269"),
                Fixtures.read("detail-no-body.html"));
        // 156790100 상세는 일부러 넣지 않음 → 404로 실패 처리
    }

    @Test
    void 목록과_상세를_읽어_저장하고_실패한_글은_건너뛴다() {
        CrawlResult result = service.crawl(1);

        assertThat(result.message()).isNull();
        assertThat(result.found()).isEqualTo(3);
        assertThat(result.saved()).isEqualTo(2);
        assertThat(result.failed()).isEqualTo(1);

        PressRelease saved = repository.findAll().stream()
                .filter(p -> p.getNewsId().equals("156790270")).findFirst().orElseThrow();
        assertThat(saved.getTitle()).isEqualTo("인공지능 학습을 위한 저작물 이용허락 표준계약서(안) 공개");
        assertThat(saved.getMinistry()).isEqualTo("문화체육관광부");
        assertThat(saved.getBody()).contains("표준계약서(안)을 공개했다.");
        assertThat(saved.getUrl()).endsWith("pressReleaseView.do?newsId=156790270");
    }

    @Test
    void 두_번째_실행에서는_이미_저장한_글의_상세를_다시_요청하지_않는다() {
        service.crawl(1);
        fetcher.requested.clear();

        CrawlResult second = service.crawl(1);

        assertThat(second.saved()).isZero();
        assertThat(second.skipped()).isEqualTo(2);
        assertThat(repository.count()).isEqualTo(2);
        assertThat(fetcher.requested)
                .doesNotContain(KoreaPolicyListParser.detailUrl(Fixtures.BASE_URL, "156790270"));
    }

    @Test
    void 새_글이_없는_페이지를_만나면_다음_페이지로_가지_않는다() {
        fetcher.pages.put(KoreaPolicyListParser.detailUrl(Fixtures.BASE_URL, "156790100"),
                Fixtures.read("detail.html"));
        service.crawl(1);
        fetcher.requested.clear();

        CrawlResult result = service.crawl(3);

        assertThat(result.pagesFetched()).isEqualTo(1);
        assertThat(fetcher.requested).doesNotContain(LIST_2);
    }

    @Test
    void robots_txt가_막으면_요청하지_않는다() {
        fetcher.pages.put(Fixtures.BASE_URL + "/robots.txt", "User-agent: *\nDisallow: /briefing/\n");

        CrawlResult result = service.crawl(1);

        assertThat(result.message()).contains("robots.txt");
        assertThat(fetcher.requested).containsExactly(Fixtures.BASE_URL + "/robots.txt");
        assertThat(repository.count()).isZero();
    }
}
