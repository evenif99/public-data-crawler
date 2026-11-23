package com.evenif.crawler.pressrelease.crawler;

import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KoreaPolicyListParserTest {

    private final KoreaPolicyListParser parser = new KoreaPolicyListParser();

    @Test
    void 목록에서_상세링크_기준으로_글을_뽑는다() {
        List<PressReleaseListItem> items = parser.parse(
                Fixtures.document("list.html", Fixtures.BASE_URL + "/briefing/pressReleaseList.do"),
                Fixtures.BASE_URL);

        // 같은 newsId 링크 2개는 1건으로, 메뉴 링크(pressReleaseList.do)는 제외
        assertThat(items).extracting(PressReleaseListItem::newsId)
                .containsExactly("156790270", "156790269", "156790100");

        PressReleaseListItem first = items.get(0);
        assertThat(first.title()).isEqualTo("인공지능 학습을 위한 저작물 이용허락 표준계약서(안) 공개");
        assertThat(first.ministry()).isEqualTo("문화체육관광부");
        assertThat(first.publishedDate()).isEqualTo(LocalDate.of(2026, 11, 23));
        // 요약 안에 날짜(2026.11.25)가 있어도 끝의 "날짜 부처"만 잘라낸다
        assertThat(first.summary()).startsWith("문화체육관광부는 2026.11.25 공청회");
        // 목록 파라미터를 뺀 주소로 저장해야 같은 글이 같은 URL이 된다
        assertThat(first.detailUrl())
                .isEqualTo("https://www.korea.kr/briefing/pressReleaseView.do?newsId=156790270");
    }

    @Test
    void 부처명에_괄호가_있고_요약이_없어도_읽는다() {
        List<PressReleaseListItem> items = parser.parse(
                Fixtures.document("list.html", Fixtures.BASE_URL), Fixtures.BASE_URL);

        PressReleaseListItem last = items.get(2);
        assertThat(last.title()).isEqualTo("국무회의 결과 브리핑");
        assertThat(last.ministry()).isEqualTo("국무조정실(국무총리비서실)");
        assertThat(last.publishedDate()).isEqualTo(LocalDate.of(2026, 11, 21));
        assertThat(last.summary()).isNull();
    }

    @Test
    void 글이_없으면_빈_목록() {
        assertThat(parser.parse(Jsoup.parse("<html><body>검색 결과가 없습니다</body></html>"),
                Fixtures.BASE_URL)).isEmpty();
    }
}
