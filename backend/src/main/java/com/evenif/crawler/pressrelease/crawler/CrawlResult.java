package com.evenif.crawler.pressrelease.crawler;

import java.time.LocalDateTime;

/**
 * 수집 1회 결과.
 *
 * @param found   목록에서 찾은 글 수
 * @param saved   새로 저장한 글 수
 * @param skipped 이미 저장돼 있어 건너뛴 글 수
 * @param failed  상세 페이지 요청·파싱에 실패한 글 수
 * @param message 중단 사유 등 (정상 종료면 null)
 */
public record CrawlResult(
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        int pagesFetched,
        int found,
        int saved,
        int skipped,
        int failed,
        String message
) {
}
