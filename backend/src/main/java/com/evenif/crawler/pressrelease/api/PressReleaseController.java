package com.evenif.crawler.pressrelease.api;

import com.evenif.crawler.pressrelease.PressReleaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.evenif.crawler.pressrelease.PressReleaseSpecs.ministryEquals;
import static com.evenif.crawler.pressrelease.PressReleaseSpecs.titleContains;

@RestController
@RequestMapping("/api/press-releases")
public class PressReleaseController {

    private static final int MAX_SIZE = 100;

    private final PressReleaseRepository repository;

    public PressReleaseController(PressReleaseRepository repository) {
        this.repository = repository;
    }

    /** 목록: 최신 게시일 순, 제목 키워드·부처 필터. page는 0부터. */
    @GetMapping
    public PageResponse<PressReleaseSummaryDto> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String ministry,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0),
                Math.min(Math.max(size, 1), MAX_SIZE),
                Sort.by(Sort.Order.desc("publishedDate"), Sort.Order.desc("newsId")));
        Page<PressReleaseSummaryDto> result = repository
                .findAll(titleContains(keyword).and(ministryEquals(ministry)), pageable)
                .map(PressReleaseSummaryDto::from);
        return PageResponse.from(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PressReleaseDetailDto> get(@PathVariable Long id) {
        return repository.findById(id)
                .map(PressReleaseDetailDto::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
