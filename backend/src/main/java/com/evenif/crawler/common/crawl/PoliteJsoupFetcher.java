package com.evenif.crawler.common.crawl;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Jsoup 기반 fetcher. 모든 요청 사이에 최소 delayMillis 간격을 둔다.
 * 여러 스레드가 동시에 호출해도 간격이 지켜지도록 synchronized로 막는다.
 */
@Component
public class PoliteJsoupFetcher implements HtmlFetcher {

    private static final Logger log = LoggerFactory.getLogger(PoliteJsoupFetcher.class);

    private final CrawlProperties props;
    private long lastRequestAt = 0L;

    public PoliteJsoupFetcher(CrawlProperties props) {
        this.props = props;
    }

    @Override
    public Document fetchDocument(String url) throws IOException {
        waitTurn();
        log.debug("GET {}", url);
        return Jsoup.connect(url)
                .userAgent(props.userAgent())
                .timeout(props.timeoutMillis())
                .get();
    }

    @Override
    public String fetchText(String url) throws IOException {
        waitTurn();
        log.debug("GET {}", url);
        return Jsoup.connect(url)
                .userAgent(props.userAgent())
                .timeout(props.timeoutMillis())
                .ignoreContentType(true)
                .execute()
                .body();
    }

    private synchronized void waitTurn() {
        long wait = lastRequestAt + props.delayMillis() - System.currentTimeMillis();
        if (wait > 0) {
            try {
                Thread.sleep(wait);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        lastRequestAt = System.currentTimeMillis();
    }
}
