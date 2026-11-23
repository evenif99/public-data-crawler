package com.evenif.crawler.pressrelease.crawler;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

import java.time.LocalDate;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 파서 공용 유틸. */
final class HtmlText {

    static final Pattern DATE = Pattern.compile("(\\d{4})[.\\-](\\d{1,2})[.\\-](\\d{1,2})");

    private static final Set<String> BLOCK_TAGS = Set.of(
            "p", "div", "li", "tr", "h1", "h2", "h3", "h4", "h5", "h6",
            "table", "ul", "ol", "section", "article");

    private HtmlText() {
    }

    /** Element.text()는 줄바꿈을 모두 없애므로, 문단·br 단위로 줄을 나눠서 꺼낸다. */
    static String withLineBreaks(Element element) {
        Element copy = element.clone();
        copy.select("script, style, noscript").remove();

        StringBuilder sb = new StringBuilder();
        NodeTraversor.traverse(new NodeVisitor() {
            @Override
            public void head(Node node, int depth) {
                if (node instanceof TextNode text) {
                    sb.append(text.text());
                } else if (node instanceof Element el && el.nameIs("br")) {
                    sb.append('\n');
                }
            }

            @Override
            public void tail(Node node, int depth) {
                if (node instanceof Element el && BLOCK_TAGS.contains(el.normalName())) {
                    sb.append('\n');
                }
            }
        }, copy);

        return sb.toString()
                .replace(' ', ' ')
                .replaceAll("[ \\t]+", " ")
                .replaceAll(" *\\n *", "\n")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }

    static LocalDate parseDate(String text) {
        if (text == null) {
            return null;
        }
        Matcher m = DATE.matcher(text);
        if (!m.find()) {
            return null;
        }
        try {
            return LocalDate.of(Integer.parseInt(m.group(1)),
                    Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)));
        } catch (RuntimeException e) {
            return null;
        }
    }

    static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
