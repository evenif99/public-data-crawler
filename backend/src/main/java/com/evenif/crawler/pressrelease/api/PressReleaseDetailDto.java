package com.evenif.crawler.pressrelease.api;

import com.evenif.crawler.pressrelease.PressRelease;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PressReleaseDetailDto(Long id, String newsId, String title, String ministry,
                                    LocalDate publishedDate, String summary, String body,
                                    String url, LocalDateTime collectedAt) {

    public static PressReleaseDetailDto from(PressRelease p) {
        return new PressReleaseDetailDto(p.getId(), p.getNewsId(), p.getTitle(),
                p.getMinistry(), p.getPublishedDate(), p.getSummary(), p.getBody(),
                p.getUrl(), p.getCollectedAt());
    }
}
