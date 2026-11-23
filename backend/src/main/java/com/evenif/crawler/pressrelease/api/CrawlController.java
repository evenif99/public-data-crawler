package com.evenif.crawler.pressrelease.api;

import com.evenif.crawler.pressrelease.crawler.CrawlAlreadyRunningException;
import com.evenif.crawler.pressrelease.crawler.CrawlResult;
import com.evenif.crawler.pressrelease.crawler.KoreaPolicyCrawlService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 수동 실행용 엔드포인트. 수집이 끝날 때까지 기다렸다가 결과를 돌려준다. */
@RestController
@RequestMapping("/api/crawl")
public class CrawlController {

    private final KoreaPolicyCrawlService crawlService;

    public CrawlController(KoreaPolicyCrawlService crawlService) {
        this.crawlService = crawlService;
    }

    @PostMapping("/korea-policy")
    public CrawlResult crawlKoreaPolicy(@RequestParam(required = false) Integer maxPages) {
        return crawlService.crawl(maxPages);
    }

    @ExceptionHandler(CrawlAlreadyRunningException.class)
    public ResponseEntity<Map<String, String>> alreadyRunning(CrawlAlreadyRunningException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
    }
}
