package com.evenif.crawler.common.crawl;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 크롤러 공통 설정 (application.yml의 crawler.*).
 *
 * @param userAgent     요청 헤더에 넣을 User-Agent
 * @param timeoutMillis 요청 1건 타임아웃
 * @param delayMillis   요청과 요청 사이 최소 간격 (서버 부담 줄이기)
 */
@ConfigurationProperties(prefix = "crawler")
public record CrawlProperties(String userAgent, int timeoutMillis, long delayMillis) {
}
