package com.example.demo.controller;

import com.example.demo.crawl.CrawlQueue;
import com.example.demo.crawl.CrawlJob;
import com.example.demo.util.MdReportParser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.util.Map;

@Controller
public class SearchController {

    private final CrawlQueue crawlQueue;

    public SearchController(CrawlQueue crawlQueue) {
        this.crawlQueue = crawlQueue;
    }

    // HOME (나중에 따로 디자인)
    @GetMapping("/")
    public String home() {
        return "home"; // home.html 만들거나, 일단 analy로 리다이렉트해도 됨
    }

    // 키워드 정밀 분석 화면 (analy.html)
    @GetMapping("/analyze")
    public String analyzePage() {
        return "analy"; // templates/analy.html
    }


 // SearchController.java

	 // 1. /search 수정: 페이지 이동 대신 키워드만 반환 (JSON)
    @PostMapping("/search")
    @ResponseBody
    public Map<String, String> search(@RequestParam("keyword") String keyword) {
        // 새로운 검색을 시작하기 전에 기존 큐를 비웁니다.
        crawlQueue.clearQueue(); 
        
        String[] sites = new String[]{"dc", "clien", "fmk", "quasar"};
        crawlQueue.add(keyword, sites);
        return Map.of("keyword", keyword, "status", "START");
    }
	
	 // 2. /result 수정: 데이터만 반환하는 API 형태로 변경 (또는 Fragment 반환)
	 @GetMapping("/api/result-data")
	 @ResponseBody
	 public Map<String, Object> getResultData(@RequestParam("keyword") String keyword) {
	     CrawlJob job = crawlQueue.get(keyword);
	     if (job == null || job.getStatus() != CrawlJob.Status.DONE) {
	         return Map.of("error", "NOT_READY");
	     }
	
	     String filePath = "data_storage/" + keyword + "/" + keyword + "_report.md";
	     Map<String, String> reportData = MdReportParser.parseReport(filePath);
	
	     return Map.of(
	         "summary", reportData.getOrDefault("summary", "내용 없음"),
	         "pros", reportData.getOrDefault("pros", "").lines().toList(),
	         "cons", reportData.getOrDefault("cons", "").lines().toList()
	     );
	 }
}
