package com.evenif.crawler.pressrelease.crawler;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 보도자료 목록 페이지 파서.
 *
 * <p>목록을 감싸는 태그의 class 이름에 기대지 않고, 상세 링크
 * (pressReleaseView.do?newsId=...)를 기준으로 항목을 찾는다.
 * 2026-11-23 확인한 링크 텍스트 구조: "제목 요약… 2026.11.23 문화체육관광부"
 */
@Component
public class KoreaPolicyListParser {

    static final String VIEW_PATH = "/briefing/pressReleaseView.do";
    private static final Pattern NEWS_ID = Pattern.compile("[?&]newsId=(\\d+)");
    /** 링크 텍스트 끝의 "날짜 부처명" */
    private static final Pattern DATE_AND_MINISTRY =
            Pattern.compile("(\\d{4}\\.\\d{1,2}\\.\\d{1,2})\\s*([^\\d.][^.]*?)?\\s*$");

    public List<PressReleaseListItem> parse(Document doc, String baseUrl) {
        Map<String, PressReleaseListItem> items = new LinkedHashMap<>();

        for (Element link : doc.select("a[href*=pressReleaseView.do]")) {
            Matcher idMatcher = NEWS_ID.matcher(link.attr("href"));
            if (!idMatcher.find()) {
                continue;
            }
            String newsId = idMatcher.group(1);
            if (items.containsKey(newsId)) {
                continue; // 같은 글로 가는 링크가 여러 개일 수 있음 (썸네일 + 제목)
            }

            String fullText = link.text().trim();
            String title = titleOf(link);
            if (title == null) {
                continue;
            }

            String ministry = null;
            String dateText = null;
            String body = fullText;
            Matcher tail = DATE_AND_MINISTRY.matcher(fullText);
            if (tail.find()) {
                dateText = tail.group(1);
                ministry = HtmlText.blankToNull(tail.group(2));
                body = fullText.substring(0, tail.start());
            }
            if (body.startsWith(title)) {
                body = body.substring(title.length());
            }

            items.put(newsId, new PressReleaseListItem(
                    newsId,
                    title,
                    ministry,
                    HtmlText.parseDate(dateText),
                    HtmlText.blankToNull(body),
                    detailUrl(baseUrl, newsId)));
        }
        return new ArrayList<>(items.values());
    }

    /** 목록 파라미터(pageIndex 등)를 뺀 상세 주소. 같은 글은 항상 같은 URL로 저장된다. */
    public static String detailUrl(String baseUrl, String newsId) {
        return baseUrl + VIEW_PATH + "?newsId=" + newsId;
    }

    private static String titleOf(Element link) {
        Element strong = link.selectFirst("strong");
        if (strong != null && !strong.text().isBlank()) {
            return strong.text().trim();
        }
        return HtmlText.blankToNull(link.attr("title"));
    }
}
