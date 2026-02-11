package com.example.demo.controller;


import com.example.demo.crawl.CrawlJob;
import com.example.demo.crawl.CrawlQueue;


import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/crawl")
public class CrawlApiController {

    private final CrawlQueue crawlQueue;

    public CrawlApiController(CrawlQueue crawlQueue) {
        this.crawlQueue = crawlQueue;
    }

    @GetMapping("/next")
    public ResponseEntity<?> next() {
        CrawlJob job = crawlQueue.poll();
        if (job == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(job);
    }
    
    @PostMapping("/done")
    public ResponseEntity<String> completeCrawl(@RequestBody CrawlResultRequest request) {
        CrawlJob job = crawlQueue.get(request.getKeyword());
        
        if (job != null) {
            // 1. 실패 처리
            if ("FAIL".equalsIgnoreCase(request.getStatus())) {
                job.setStatus(CrawlJob.Status.FAILED);
                System.out.println("❌ 실패 처리됨: " + request.getKeyword());
                return ResponseEntity.ok("Marked as FAILED");
            }

            // 2. 성공 처리
            job.setStatus(CrawlJob.Status.DONE);
            
            // ★★★ [여기 추가] MD 파일 저장 로직 ★★★
            if (request.getReportContent() != null && !request.getReportContent().isEmpty()) {
                saveMdFile(request.getKeyword(), request.getReportContent());
            }

            return ResponseEntity.ok("Done & Saved (MD)");
        }
        
        return ResponseEntity.status(404).body("Job not found");
    }

    // 파일 저장 메서드
    private void saveMdFile(String keyword, String content) {
        try {
            String folderPath = "data_storage/" + keyword;
            Files.createDirectories(Paths.get(folderPath));
            String filePath = folderPath + "/" + keyword + "_report.md";
            Files.write(Paths.get(filePath), content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            System.out.println("✅ 리포트 저장 완료: " + filePath);
        } catch (Exception e) {
            System.err.println("❌ 저장 실패: " + e.getMessage());
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

}
