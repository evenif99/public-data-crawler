package com.evenif.crawler.pressrelease;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 정책브리핑(korea.kr) 보도자료 1건.
 * newsId(사이트 고유 번호)에 유니크 제약을 걸어 같은 글이 두 번 저장되지 않게 한다.
 */
@Entity
@Table(
        name = "press_release",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_press_release_news_id", columnNames = "news_id"),
        indexes = @Index(
                name = "idx_press_release_published", columnList = "published_date")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PressRelease {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "news_id", nullable = false, length = 30)
    private String newsId;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(length = 100)
    private String ministry;

    @Column(name = "published_date")
    private LocalDate publishedDate;

    /** 목록에 노출되는 요약문. 본문이 첨부파일(HWP/PDF)뿐인 글이 있어 함께 저장한다. */
    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(name = "collected_at", nullable = false)
    private LocalDateTime collectedAt;

    public PressRelease(String newsId, String title, String ministry,
                        LocalDate publishedDate, String summary, String body,
                        String url, LocalDateTime collectedAt) {
        this.newsId = newsId;
        this.title = title;
        this.ministry = ministry;
        this.publishedDate = publishedDate;
        this.summary = summary;
        this.body = body;
        this.url = url;
        this.collectedAt = collectedAt;
    }
}
