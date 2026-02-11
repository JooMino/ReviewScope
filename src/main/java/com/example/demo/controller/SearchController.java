package com.example.demo.controller;

import com.example.demo.crawl.CrawlQueue;
import com.example.demo.crawl.CrawlJob;
import com.example.demo.util.MdReportParser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.util.Map;

@Controller
public class SearchController {

    private final CrawlQueue crawlQueue;

    public SearchController(CrawlQueue crawlQueue) {
        this.crawlQueue = crawlQueue;
    }

    // 메인 페이지
    @GetMapping("/")
    public String index() {
        return "index";
    }

    // 검색 요청 처리
    @PostMapping("/search")
    public String search(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "sites", required = false) String[] sites,
            Model model
    ) {
        if (sites == null || sites.length == 0) {
            sites = new String[]{"dc", "clien", "fmk", "quasar"};
        }

        // 큐에 작업 등록
        crawlQueue.add(keyword, sites);

        model.addAttribute("keyword", keyword);
        return "waiting";   // 대기 화면으로 이동
    }
    
    // 결과 페이지
    @GetMapping("/result")
    public String result(
            @RequestParam("keyword") String keyword,
            Model model
    ) {
        CrawlJob job = crawlQueue.get(keyword);

        // 1. 작업 진행 중 or 실패 체크
        if (job == null) {
            model.addAttribute("keyword", keyword);
            return "waiting";
        }
        
        // 실패했다면 에러 페이지나 알림을 띄울 수도 있음 (여기선 일단 대기로 처리하거나 분기 가능)
        if (job.getStatus() == CrawlJob.Status.FAILED) {
             model.addAttribute("error", "분석 작업이 실패했습니다.");
             return "index"; // 메인으로 튕기기
        }

        if (job.getStatus() != CrawlJob.Status.DONE) {
            model.addAttribute("keyword", keyword);
            return "waiting";
        }

        // 2. 작업 완료됨! (Status.DONE)
        // 이제 CSV가 아니라 MD 파일을 읽습니다.
        // 저장 경로: data_storage/키워드/키워드_report.md
        String filePath = "data_storage/" + keyword + "/" + keyword + "_report.md";
        
        File file = new File(filePath);
        if (!file.exists()) {
            // 완료는 됐는데 파일이 없다? -> 에러 처리
            model.addAttribute("error", "리포트 파일이 없습니다.");
            return "index";
        }

        // 3. MD 파서로 내용 쪼개기
        Map<String, String> reportData = MdReportParser.parseReport(filePath);

        // 4. HTML로 데이터 전달
        // 맵에 담긴 pros, cons, models, fullContent 등을 모델에 추가
        model.addAttribute("keyword", keyword);
        model.addAttribute("pros", reportData.getOrDefault("pros", "내용 없음"));
        model.addAttribute("cons", reportData.getOrDefault("cons", "내용 없음"));
        model.addAttribute("models", reportData.getOrDefault("models", "내용 없음"));
        model.addAttribute("summary", reportData.getOrDefault("summary", "내용 없음")); // 요약이나 전체 내용

        // 필요하다면 원본 전체도 보냄
        // model.addAttribute("fullReport", reportData.get("fullContent"));

        return "result"; // result.html (수정 필요)
    }
}