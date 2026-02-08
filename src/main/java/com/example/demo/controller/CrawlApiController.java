package com.example.demo.controller;

import com.example.demo.util.ReviewDto;
import com.example.demo.crawl.CrawlJob;
import com.example.demo.crawl.CrawlQueue;

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
            return ResponseEntity.ok("Done & Saved");
            saveResultToCsv(request.getKeyword(), request.getResults());         
            // ★★★ 추가: 받은 데이터를 CSV 파일로 저장하는 코드 ★★★
            job.setStatus(CrawlJob.Status.DONE);
            
        if (job != null) {
        CrawlJob job = crawlQueue.get(request.getKeyword());
    public ResponseEntity<String> completeCrawl(@RequestBody CrawlResultRequest request) {
        }
        return ResponseEntity.status(404).body("Job not found");
    }
    
    @GetMapping("/status")
    public Map<String, String> status(@RequestParam("keyword") String keyword) {
        CrawlJob job = crawlQueue.get(keyword);
        if (job == null) {
            return Map.of("status", "PENDING"); // 혹은 에러 처리
        }
        return Map.of("status", job.getStatus().name());
    }
    private void saveResultToCsv(String keyword, List<ReviewDto> results) {
        try {
            // 저장 경로: 프로젝트 루트 / data_storage / 키워드 / result.csv
            // (폴더 없으면 만듦)
            String folderPath = "data_storage/" + keyword;
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get(folderPath));
            
            String filePath = folderPath + "/result.csv";
            try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(filePath))) {
                // 헤더 쓰기 (CsvReader가 읽을 순서대로!)
                // file, chars, type, model, summary, sentiment
                pw.println("file,chars,type,model,summary,sentiment");
                
                for (ReviewDto dto : results) {
                    
                    // ★ DTO에서 값 꺼내기 (null 안전 처리)
                    String title = (dto.getTitle() != null) ? dto.getTitle() : "";
                    String link = (dto.getLink() != null) ? dto.getLink() : "";
                    String summary = (dto.getSummary() != null) ? dto.getSummary() : "";
                    String sentiment = (dto.getSentiment() != null) ? dto.getSentiment() : "";
                    
                    // 콤마(,)가 내용에 있으면 깨지니까 제거하거나 대체
                    summary = summary.replace(",", " "); 
                    
                    // CSV 한 줄 쓰기
                    // 예: clien, 0, post, http://..., 요약내용, 긍정
                    pw.println(title + ",0,post," + link + "," + summary + "," + sentiment);
                }
            }
            System.out.println("✅ CSV 파일 저장 완료: " + filePath);
            
        } catch (Exception e) {
            System.err.println("❌ CSV 저장 실패: " + e.getMessage());
            e.printStackTrace();
       }
    }
}
