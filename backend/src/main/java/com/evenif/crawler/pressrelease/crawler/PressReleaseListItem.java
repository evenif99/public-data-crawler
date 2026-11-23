package com.evenif.crawler.pressrelease.crawler;

import java.time.LocalDate;

/** 목록 페이지에서 읽은 1건. 상세 페이지 파싱 실패 시 대체값으로도 쓴다. */
public record PressReleaseListItem(
        String newsId,
        String title,
        String ministry,
        LocalDate publishedDate,
        String summary,
        String detailUrl
) {
}
