package com.evenif.crawler.pressrelease;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PressReleaseRepository
        extends JpaRepository<PressRelease, Long>, JpaSpecificationExecutor<PressRelease> {

    boolean existsByNewsId(String newsId);
}
