package com.evenif.crawler.pressrelease.crawler;

import java.time.LocalDate;

/** 상세 페이지에서 읽은 값. 찾지 못한 항목은 null. */
public record PressReleaseDetail(
        String title,
        String ministry,
        LocalDate publishedDate,
        String body
) {
}
