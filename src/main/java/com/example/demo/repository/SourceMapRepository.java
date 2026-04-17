package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.domain.SourceMap;

import java.util.List;
import java.util.Optional;

public interface SourceMapRepository extends JpaRepository<SourceMap, Long> {
    Optional<SourceMap> findTopByKeywordAndHashValueOrderByCreatedAtDesc(String keyword, String hashValue);
    List<SourceMap> findByKeywordAndHashValueIn(String keyword, List<String> hashValues);
}