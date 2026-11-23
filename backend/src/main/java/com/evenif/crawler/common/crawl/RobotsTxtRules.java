package com.evenif.crawler.common.crawl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * robots.txt 규칙 (User-agent: * 그룹만 사용).
 * 가장 길게 일치하는 규칙을 따르고, 길이가 같으면 Allow가 이긴다 (RFC 9309).
 */
public final class RobotsTxtRules {

    private record Rule(boolean allow, String path, Pattern pattern) {
    }

    private final List<Rule> rules;

    private RobotsTxtRules(List<Rule> rules) {
        this.rules = rules;
    }

    public static RobotsTxtRules allowAll() {
        return new RobotsTxtRules(List.of());
    }

    public static RobotsTxtRules parse(String robotsTxt) {
        List<Rule> rules = new ArrayList<>();
        boolean inStarGroup = false;
        boolean lastWasAgent = false;

        for (String raw : robotsTxt.split("\\R")) {
            String line = raw.replaceFirst("#.*$", "").trim();
            int colon = line.indexOf(':');
            if (colon < 0) {
                continue;
            }
            String key = line.substring(0, colon).trim().toLowerCase(Locale.ROOT);
            String value = line.substring(colon + 1).trim();

            if (key.equals("user-agent")) {
                // 연속된 User-agent 줄은 같은 그룹
                boolean star = value.equals("*");
                inStarGroup = lastWasAgent ? (inStarGroup || star) : star;
                lastWasAgent = true;
                continue;
            }
            lastWasAgent = false;
            if (!inStarGroup || value.isEmpty()) {
                continue; // 빈 Disallow는 "전부 허용"
            }
            if (key.equals("allow") || key.equals("disallow")) {
                rules.add(new Rule(key.equals("allow"), value, toPattern(value)));
            }
        }
        return new RobotsTxtRules(rules);
    }

    /** @param pathAndQuery 예: /briefing/pressReleaseList.do?pageIndex=1 */
    public boolean isAllowed(String pathAndQuery) {
        Rule best = null;
        for (Rule rule : rules) {
            if (!rule.pattern().matcher(pathAndQuery).lookingAt()) {
                continue;
            }
            if (best == null
                    || rule.path().length() > best.path().length()
                    || (rule.path().length() == best.path().length() && rule.allow())) {
                best = rule;
            }
        }
        return best == null || best.allow();
    }

    private static Pattern toPattern(String path) {
        StringBuilder regex = new StringBuilder();
        for (char c : path.toCharArray()) {
            if (c == '*') {
                regex.append(".*");
            } else if (c == '$') {
                regex.append("$");
            } else {
                regex.append(Pattern.quote(String.valueOf(c)));
            }
        }
        return Pattern.compile(regex.toString());
    }
}
