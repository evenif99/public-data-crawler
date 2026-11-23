package com.evenif.crawler.pressrelease.crawler;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 보도자료 상세 페이지 파서.
 *
 * <p>제목은 &lt;title&gt; 형식("제목 - 보도자료 | 브리핑룸 | 대한민국 정책브리핑")을
 * 2026-11-23에 확인했다. 본문·부처·날짜 영역의 class 이름은 클라우드에서
 * 원본 HTML을 받지 못해 아직 확인하지 못했으므로 후보 선택자를 차례로 시도한다.
 * 찾지 못하면 null을 돌려주고, 서비스에서 목록 값으로 대체한다.
 */
@Component
public class KoreaPolicyDetailParser {

    private static final String TITLE_SUFFIX = " - 보도자료";

    static final List<String> BODY_SELECTORS = List.of(
            "div.view_cont", "div.article_body", "div.view_content",
            "div.view_con", "div.cont_view", "#articleBody");

    static final List<String> MINISTRY_SELECTORS = List.of(
            "div.info span.ministry", "div.article_head .info a",
            "div.view_title .info a", ".info a[href*=repCode]");

    static final List<String> DATE_SELECTORS = List.of(
            "div.info span.date", "div.article_head .info span",
            "div.view_title .info span", ".info span");

    static final List<String> TITLE_SELECTORS = List.of(
            "div.article_head h1", "div.view_title h1", "div.article_head h2");

    public PressReleaseDetail parse(Document doc) {
        return new PressReleaseDetail(
                title(doc),
                firstText(doc, MINISTRY_SELECTORS),
                date(doc),
                body(doc));
    }

    private static String title(Document doc) {
        String pageTitle = doc.title();
        int idx = pageTitle.lastIndexOf(TITLE_SUFFIX);
        if (idx > 0) {
            return pageTitle.substring(0, idx).trim();
        }
        return firstText(doc, TITLE_SELECTORS);
    }

    private static java.time.LocalDate date(Document doc) {
        for (String selector : DATE_SELECTORS) {
            for (Element el : doc.select(selector)) {
                var date = HtmlText.parseDate(el.text());
                if (date != null) {
                    return date;
                }
            }
        }
        return null;
    }

    private static String body(Document doc) {
        for (String selector : BODY_SELECTORS) {
            Element el = doc.selectFirst(selector);
            if (el != null) {
                String text = HtmlText.withLineBreaks(el);
                if (!text.isBlank()) {
                    return text;
                }
            }
        }
        return null;
    }

    private static String firstText(Document doc, List<String> selectors) {
        for (String selector : selectors) {
            Element el = doc.selectFirst(selector);
            if (el != null && !el.text().isBlank()) {
                return el.text().trim();
            }
        }
        return null;
    }
}
