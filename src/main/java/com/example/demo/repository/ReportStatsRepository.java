package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.domain.ReportStats;

public interface ReportStatsRepository extends JpaRepository<ReportStats, Long> {
	Optional<ReportStats> findTopByKeywordOrderByCreatedAtDesc(String keyword);
}