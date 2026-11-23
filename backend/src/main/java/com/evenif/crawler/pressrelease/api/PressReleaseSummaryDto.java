package com.evenif.crawler.pressrelease.api;

import com.evenif.crawler.pressrelease.PressRelease;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PressReleaseSummaryDto(Long id, String newsId, String title, String ministry,
                                     LocalDate publishedDate, String summary, String url,
                                     LocalDateTime collectedAt) {

    public static PressReleaseSummaryDto from(PressRelease p) {
        return new PressReleaseSummaryDto(p.getId(), p.getNewsId(), p.getTitle(),
                p.getMinistry(), p.getPublishedDate(), p.getSummary(), p.getUrl(),
                p.getCollectedAt());
    }
}
