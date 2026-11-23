package com.evenif.crawler.pressrelease;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/** 목록 API 검색 조건. 값이 비어 있으면 조건을 걸지 않는다. */
public final class PressReleaseSpecs {

    private PressReleaseSpecs() {
    }

    public static Specification<PressRelease> titleContains(String keyword) {
        return (root, query, cb) -> StringUtils.hasText(keyword)
                ? cb.like(cb.lower(root.get("title")),
                        "%" + keyword.trim().toLowerCase() + "%")
                : null;
    }

    public static Specification<PressRelease> ministryEquals(String ministry) {
        return (root, query, cb) -> StringUtils.hasText(ministry)
                ? cb.equal(root.get("ministry"), ministry.trim())
                : null;
    }
}
