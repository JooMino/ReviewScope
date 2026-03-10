package com.example.demo.controller;

import com.example.demo.crawl.*;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/crawl")
public class CrawlApiController {

    private final CrawlQueue crawlQueue;
    private final CrawlReportRepository crawlReportRepository;

    public CrawlApiController(CrawlQueue crawlQueue, CrawlReportRepository crawlReportRepository) {
        this.crawlQueue = crawlQueue;
        this.crawlReportRepository = crawlReportRepository;
    }

    @GetMapping("/next")
    public ResponseEntity<?> next() {
        CrawlJob job = crawlQueue.poll();
        if (job == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(job);
    }
    @GetMapping("/exists")
    public Map<String, Object> reportExists(@RequestParam("keyword") String keyword) {
        boolean exists = crawlReportRepository.findRecentByKeyword(
                keyword,
                java.time.LocalDateTime.now().minusDays(7)
        ).isPresent();

        return Map.of(
            "keyword", keyword,
            "exists", exists
        );
    }
    @PostMapping("/done")
    public ResponseEntity<String> completeCrawl(@RequestBody CrawlResultRequest request) {
        CrawlJob job = crawlQueue.get(request.getKeyword());
        
        if (job != null) {
            String keyword = request.getKeyword();
            
            // ★★★ 정상 처리 ★★★
            if ("FAIL".equalsIgnoreCase(request.getStatus())) {
                job.setStatus(CrawlJob.Status.FAILED);
                saveToDb(keyword, null, CrawlJob.Status.FAILED);
                System.out.println("❌ 실패 처리: " + keyword);
                return ResponseEntity.ok("Marked as FAILED");
            }

            job.setStatus(CrawlJob.Status.DONE);
            saveToDb(keyword, request.getReportContent(), CrawlJob.Status.DONE);
            System.out.println(" 신규 데이터 저장: " + keyword);
            return ResponseEntity.ok("Done & Saved to DB");
        }
        
        return ResponseEntity.status(404).body("Job not found");
    }

    // DB 저장 메서드 (upsert)
    private void saveToDb(String keyword, String content, CrawlJob.Status status) {
        try {
            crawlReportRepository.findByKeyword(keyword)
                .ifPresentOrElse(
                    existing -> {
                        existing.setReportContent(content);
                        existing.setStatus(status);
                        existing.setCreatedAt(LocalDateTime.now());
                        crawlReportRepository.save(existing);
                        System.out.println("기존 데이터 업데이트: " + keyword);
                    },
                    () -> {
                        CrawlReport report = new CrawlReport(keyword, content, status);
                        crawlReportRepository.save(report);
                        System.out.println(" 신규 데이터 생성: " + keyword);
                    }
                );
        } catch (Exception e) {
            System.err.println("❌ DB 저장 실패 [" + keyword + "]: " + e.getMessage());
        }
    }
    
    @GetMapping("/status")
    public Map<String, String> status(@RequestParam("keyword") String keyword) {
        CrawlJob job = crawlQueue.get(keyword);
        if (job == null) {
            return Map.of("status", "PENDING");
        }
        return Map.of("status", job.getStatus().name());
    }

    // 리포트 조회 API
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