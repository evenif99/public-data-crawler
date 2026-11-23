package com.evenif.crawler.pressrelease.crawler;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 정책브리핑 수집 설정 (application.yml의 crawler.korea-policy.*).
 *
 * @param baseUrl  사이트 주소. 테스트·로컬 확인 때 다른 서버로 바꿀 수 있다.
 * @param maxPages 한 번 실행할 때 최대로 읽을 목록 페이지 수
 */
@ConfigurationProperties(prefix = "crawler.korea-policy")
public record KoreaPolicyProperties(String baseUrl, int maxPages) {
}
