package com.example.demo.service;
import com.example.demo.domain.ReportStats;
import com.example.demo.repository.ReportStatsRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ReportStatsService {

    private final ReportStatsRepository reportStatsRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ReportStatsService(ReportStatsRepository reportStatsRepository) {
        this.reportStatsRepository = reportStatsRepository;
    }

    public void saveStats(String keyword, String reportContent, LocalDateTime createdAt) {
        try {
            JsonNode root = objectMapper.readTree(reportContent);

            // 🔥 여기 JSON 구조 맞게 수정 가능
            int mentionCount = root.path("mention_count").asInt(0);

            int positiveCount = root.path("positive").path("count").asInt(0);
            int negativeCount = root.path("negative").path("count").asInt(0);

            ReportStats stats = new ReportStats();
            stats.setKeyword(keyword);
            stats.setCreatedAt(createdAt);
            stats.setMentionCount(mentionCount);
            stats.setPositiveCount(positiveCount);
            stats.setNegativeCount(negativeCount);

            reportStatsRepository.save(stats);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}