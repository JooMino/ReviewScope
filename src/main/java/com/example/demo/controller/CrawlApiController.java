package com.example.demo.controller;

import com.example.demo.util.ReviewDto;
import com.example.demo.crawl.CrawlJob;
import com.example.demo.crawl.CrawlQueue;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
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
            job.setStatus(CrawlJob.Status.DONE);
            
            // ★★★ 변경: CSV 대신 Markdown으로 저장 ★★★
            saveResultToMarkdown(request.getKeyword(), request.getResults());         
            return ResponseEntity.ok("Done & Saved (MD)");
        }
        return ResponseEntity.status(404).body("Job not found");
    }
    
    @GetMapping("/status")
    public Map<String, String> status(@RequestParam("keyword") String keyword) {
        CrawlJob job = crawlQueue.get(keyword);
        if (job == null) {
            return Map.of("status", "PENDING");
        }
        return Map.of("status", job.getStatus().name());
    }

    // ★★★ 변경된 저장 로직: .md 파일 생성 ★★★
    private void saveResultToMarkdown(String keyword, List<ReviewDto> results) {
        try {
            // 저장 경로: data_storage / 키워드 / result.md
            String folderPath = "data_storage/" + keyword;
            Files.createDirectories(Paths.get(folderPath));
            
            // 확장자를 .md로 변경
            String filePath = folderPath + "/" + keyword + "_result.md";

            try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
                // 마크다운 헤더 작성
                pw.println("# " + keyword + " 분석 결과 리포트");
                pw.println("Generated at: " + java.time.LocalDateTime.now());
                pw.println("Total items: " + results.size());
                pw.println(); // 빈 줄
                pw.println("---"); 
                pw.println(); 

                for (ReviewDto dto : results) {
                    // DTO 값 꺼내기 (null 처리)
                    String title = (dto.getTitle() != null) ? dto.getTitle() : "제목 없음";
                    String link = (dto.getLink() != null) ? dto.getLink() : "#";
                    String summary = (dto.getSummary() != null) ? dto.getSummary() : "내용 없음";
                    String sentiment = (dto.getSentiment() != null) ? dto.getSentiment() : "미분류";
                    
                    // 마크다운 형식으로 작성
                    // ### 제목
                    // - **링크**: [바로가기](url)
                    // - **감성**: 긍정
                    // - **요약**: 내용...
                    
                    pw.println("### " + title);
                    pw.println("- **링크**: " + link);
                    pw.println("- **감성**: " + sentiment);
                    pw.println("- **요약**:");
                    pw.println(summary); // 요약은 내용이 길 수 있으므로 줄바꿈 후 출력
                    pw.println();
                    pw.println("---"); // 구분선
                    pw.println();
                }
            }
            System.out.println("✅ MD 파일 저장 완료: " + filePath);
            
        } catch (Exception e) {
            System.err.println("❌ MD 저장 실패: " + e.getMessage());
            e.printStackTrace();
       }
    }
}
