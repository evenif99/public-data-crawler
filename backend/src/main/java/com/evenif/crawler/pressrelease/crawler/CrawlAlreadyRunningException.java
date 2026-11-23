package com.evenif.crawler.pressrelease.crawler;

public class CrawlAlreadyRunningException extends RuntimeException {

    public CrawlAlreadyRunningException() {
        super("이미 수집이 실행 중입니다.");
    }
}
