package com.example.demo.controller;

import com.example.demo.crawl.*;
import com.example.demo.domain.CrawlReport;
import com.example.demo.domain.SourceMap;
import com.example.demo.dto.CrawlResultRequest;
import com.example.demo.dto.SourceItem;
import com.example.demo.dto.SourceMapRequest;
import com.example.demo.repository.CrawlReportRepository;
import com.example.demo.repository.SourceMapRepository;
import com.example.demo.service.ReportStatsService;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/crawl")
public class CrawlApiController {
	
    private final CrawlQueue crawlQueue;
    private final CrawlReportRepository crawlReportRepository;
    private final SourceMapRepository sourceMapRepository;
    private final ReportStatsService reportStatsService;
    public CrawlApiController(
            CrawlQueue crawlQueue,
            CrawlReportRepository crawlReportRepository,
            SourceMapRepository sourceMapRepository,
            ReportStatsService reportStatsService 
    ) {
        this.crawlQueue = crawlQueue;
        this.crawlReportRepository = crawlReportRepository;
        this.sourceMapRepository = sourceMapRepository;
        this.reportStatsService = reportStatsService;
    }

    @GetMapping("/next")
    public ResponseEntity<?> next() {
        CrawlJob job = crawlQueue.poll();
        if (job == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(job);
    }

    @GetMapping("/exists")
    public Map<String, Object> reportExists(@RequestParam("keyword") String keyword) {
        boolean exists = crawlReportRepository.findValidRecentReport(
                keyword,
                LocalDateTime.now().minusDays(30)
        ).isPresent();

        return Map.of(
                "keyword", keyword,
                "exists", exists
        );
    }

    @PostMapping("/done")
    @ResponseBody
    public String done(@RequestBody CrawlResultRequest request) {
        String keyword = request.getKeyword();
        String incomingStatus = request.getStatus();
        String incomingContent = request.getReportContent();

        CrawlJob job = crawlQueue.get(keyword);
        if (job != null) {
            if ("FAIL".equalsIgnoreCase(incomingStatus)) {
                job.setStatus(CrawlJob.Status.FAILED);
            } else if ("SKIPPED".equalsIgnoreCase(incomingStatus)) {
                job.setStatus(CrawlJob.Status.SKIPPED);
            } else {
                job.setStatus(CrawlJob.Status.DONE);
            }
        }

        // SUCCESS일 때만 report 저장
        if ("SUCCESS".equalsIgnoreCase(incomingStatus)) {
            if (incomingContent == null || incomingContent.trim().isEmpty()) {
                return "SUCCESS but empty reportContent - not saved";
            }

            CrawlReport report = crawlReportRepository.findByKeyword(keyword)
                    .orElseGet(() -> {
                        CrawlReport newReport = new CrawlReport();
                        newReport.setKeyword(keyword);
                        return newReport;
                    });

            report.setReportContent(incomingContent);
            report.setStatus(CrawlJob.Status.DONE);
            report.setCreatedAt(LocalDateTime.now());

            crawlReportRepository.save(report);
            reportStatsService.saveStats(keyword, incomingContent, report.getCreatedAt());
            return "SUCCESS report saved";
        }

        // FAIL / SKIPPED는 report 테이블에 저장하지 않음
        return "Status updated only: " + incomingStatus;
    }

    @PostMapping("/source-map")
    @ResponseBody
    public String saveSourceMap(
            @RequestParam("keyword") String keyword,
            @RequestBody SourceMapRequest request
    ) {
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            return "EMPTY_SOURCE_MAP";
        }

        for (SourceItem item : request.getItems()) {
            if (item == null) continue;
            if (item.getHash() == null || item.getHash().trim().isEmpty()) continue;
            if (item.getUrl() == null || item.getUrl().trim().isEmpty()) continue;

            SourceMap entity = new SourceMap();
            entity.setKeyword(keyword);
            entity.setHashValue(item.getHash().trim());
            entity.setUrl(item.getUrl().trim());

            sourceMapRepository.save(entity);
        }

        return "SOURCE_MAP_SAVED";
    }

    @GetMapping("/source-map/url")
    @ResponseBody
    public ResponseEntity<?> getSourceUrl(
            @RequestParam("keyword") String keyword,
            @RequestParam("hash") String hash
    ) {
        return sourceMapRepository
                .findTopByKeywordAndHashValueOrderByCreatedAtDesc(keyword, hash)
                .<ResponseEntity<?>>map(row -> ResponseEntity.ok(
                        Map.of(
                                "keyword", row.getKeyword(),
                                "hash", row.getHashValue(),
                                "url", row.getUrl()
                        )
                ))
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/status")
    public Map<String, String> status(@RequestParam("keyword") String keyword) {
        CrawlJob job = crawlQueue.get(keyword);
        if (job == null) {
            return Map.of("status", "PENDING");
        }
        return Map.of("status", job.getStatus().name());
    }

    @GetMapping("/report")
    public ResponseEntity<?> getReport(@RequestParam("keyword") String keyword) {
        return crawlReportRepository.findByKeyword(keyword)
                .map(report -> ResponseEntity.ok(Map.of(
                        "keyword", report.getKeyword(),
                        "status", report.getStatus().name(),
                        "reportContent", report.getReportContent(),
                        "createdAt", report.getCreatedAt()
                )))
                .orElse(ResponseEntity.noContent().build());
    }
}