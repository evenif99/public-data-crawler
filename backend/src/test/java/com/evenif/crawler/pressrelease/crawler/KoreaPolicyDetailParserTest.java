package com.evenif.crawler.pressrelease.crawler;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class KoreaPolicyDetailParserTest {

    private final KoreaPolicyDetailParser parser = new KoreaPolicyDetailParser();

    @Test
    void 상세_페이지에서_제목_부처_날짜_본문을_읽는다() {
        PressReleaseDetail detail = parser.parse(Fixtures.document("detail.html",
                Fixtures.BASE_URL + "/briefing/pressReleaseView.do?newsId=156790270"));

        // 사이트 공통 꼬리(" - 보도자료 | ...")는 제거
        assertThat(detail.title()).isEqualTo("인공지능 학습을 위한 저작물 이용허락 표준계약서(안) 공개");
        assertThat(detail.ministry()).isEqualTo("문화체육관광부");
        assertThat(detail.publishedDate()).isEqualTo(LocalDate.of(2026, 11, 23));
        // 문단·br은 줄바꿈으로, script는 제거
        assertThat(detail.body()).isEqualTo("""
                - 11월 25일 공청회 개최, 업계 의견 수렴
                문화체육관광부는 인공지능 학습에 저작물을 이용할 때
                참고할 표준계약서(안)을 공개했다.

                자세한 내용은 첨부파일을 참고하기 바란다.""");
    }

    @Test
    void 본문_영역이_없으면_body는_null() {
        PressReleaseDetail detail = parser.parse(Fixtures.document("detail-no-body.html",
                Fixtures.BASE_URL));

        assertThat(detail.title()).isEqualTo("첨부파일만 있는 보도자료");
        assertThat(detail.body()).isNull();
        assertThat(detail.ministry()).isNull();
    }
}
