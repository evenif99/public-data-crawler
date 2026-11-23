package com.evenif.crawler.common.crawl;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RobotsTxtRulesTest {

    @Test
    void 전체_허용() {
        RobotsTxtRules rules = RobotsTxtRules.parse("User-agent: *\nAllow: /\n");
        assertThat(rules.isAllowed("/briefing/pressReleaseList.do?pageIndex=1")).isTrue();
    }

    @Test
    void 다른_봇_그룹의_규칙은_무시하고_별표_그룹만_적용() {
        RobotsTxtRules rules = RobotsTxtRules.parse("""
                User-agent: Googlebot
                Disallow: /

                User-agent: *
                Disallow: /admin/
                Allow: /admin/public/
                """);
        assertThat(rules.isAllowed("/briefing/pressReleaseView.do")).isTrue();
        assertThat(rules.isAllowed("/admin/secret")).isFalse();
        assertThat(rules.isAllowed("/admin/public/notice")).isTrue(); // 더 긴 규칙 우선
    }

    @Test
    void 와일드카드와_끝표시() {
        RobotsTxtRules rules = RobotsTxtRules.parse("""
                User-agent: *
                Disallow: /*.pdf$
                Disallow: /search*
                """);
        assertThat(rules.isAllowed("/files/a.pdf")).isFalse();
        assertThat(rules.isAllowed("/files/a.pdf?x=1")).isTrue();
        assertThat(rules.isAllowed("/searchResult.do")).isFalse();
    }

    @Test
    void 전체_차단과_빈_Disallow() {
        assertThat(RobotsTxtRules.parse("User-agent: *\nDisallow: /").isAllowed("/a")).isFalse();
        assertThat(RobotsTxtRules.parse("User-agent: *\nDisallow:").isAllowed("/a")).isTrue();
    }
}
