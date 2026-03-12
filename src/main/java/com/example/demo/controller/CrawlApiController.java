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
    @ResponseBody
    public String done(@RequestBody CrawlResultRequest request) {
        String keyword = request.getKeyword();
        String incomingStatus = request.getStatus();
        String incomingContent = request.getReportContent();

        CrawlJob job = crawlQueue.get(keyword);
        if (job != null) {
            if ("FAIL".equalsIgnoreCase(incomingStatus)) {
                job.setStatus(CrawlJob.Status.FAILED);
            } else {
                job.setStatus(CrawlJob.Status.DONE);
            }
        }

        CrawlReport report = crawlReportRepository.findByKeyword(keyword)
            .orElseGet(() -> {
                CrawlReport newReport = new CrawlReport();
                newReport.setKeyword(keyword);
                return newReport;
            });

        if ("SUCCESS".equalsIgnoreCase(incomingStatus)) {
            if (incomingContent != null && !incomingContent.trim().isEmpty()) {
                report.setReportContent(incomingContent);
            } else {
                System.out.println("SUCCESS인데 reportContent가 비어 있음 -> 기존 내용 유지");
            }
            report.setStatus(CrawlJob.Status.DONE);
        }
        else if ("SKIPPED".equalsIgnoreCase(incomingStatus)) {
            System.out.println("SKIPPED 수신 -> 기존 reportContent 유지");
            report.setStatus(CrawlJob.Status.SKIPPED);
        }
        else if ("FAIL".equalsIgnoreCase(incomingStatus)) {
            System.out.println("FAIL 수신 -> reportContent 유지, 상태만 실패로 저장");
            report.setStatus(CrawlJob.Status.FAILED);
        }
        else {
            System.out.println("알 수 없는 상태 수신: " + incomingStatus);
            report.setStatus(CrawlJob.Status.FAILED);
        }

        report.setCreatedAt(LocalDateTime.now());

        crawlReportRepository.save(report);

        return "Done & Saved to DB";
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