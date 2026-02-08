package com.example.demo.controller;

import com.example.demo.crawl.CrawlJob;
import com.example.demo.crawl.CrawlQueue;

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
    
    // 경로 수정: /api/crawl/done
    @PostMapping("/done")
    public void completeJob(@RequestBody CrawlResultRequest request) {
        CrawlJob job = crawlQueue.get(request.getKeyword());
        if (job != null) {
            job.setStatus(CrawlJob.Status.DONE);
            job.setResults(request.getResults()); // <--- 서버 메모리에 결과 저장!
        }
    }

    @GetMapping("/status")
    public Map<String, String> status(@RequestParam("keyword") String keyword) {
        CrawlJob job = crawlQueue.get(keyword);
        if (job == null) {
            return Map.of("status", "PENDING"); // 혹은 에러 처리
        }
        return Map.of("status", job.getStatus().name());
    }

}
