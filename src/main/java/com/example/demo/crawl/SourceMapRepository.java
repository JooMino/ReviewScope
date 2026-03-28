package com.example.demo.crawl;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SourceMapRepository extends JpaRepository<SourceMap, Long> {
    Optional<SourceMap> findTopByKeywordAndHashValueOrderByCreatedAtDesc(String keyword, String hashValue);
}