package com.evenif.crawler.common.crawl;

import org.jsoup.nodes.Document;

import java.io.IOException;

/** HTTP로 페이지를 가져오는 역할. 테스트에서는 가짜 구현으로 바꿔 끼운다. */
public interface HtmlFetcher {

    Document fetchDocument(String url) throws IOException;

    /** robots.txt처럼 HTML이 아닌 텍스트를 가져온다. 404 등은 {@link org.jsoup.HttpStatusException}. */
    String fetchText(String url) throws IOException;
}
