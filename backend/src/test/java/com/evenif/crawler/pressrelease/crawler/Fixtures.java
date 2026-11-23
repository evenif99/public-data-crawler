package com.evenif.crawler.pressrelease.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

final class Fixtures {

    static final String BASE_URL = "https://www.korea.kr";

    private Fixtures() {
    }

    static String read(String name) {
        try (InputStream in = Fixtures.class.getResourceAsStream("/fixtures/korea-policy/" + name)) {
            if (in == null) {
                throw new IllegalArgumentException("fixture 없음: " + name);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static Document document(String name, String url) {
        return Jsoup.parse(read(name), url);
    }
}
