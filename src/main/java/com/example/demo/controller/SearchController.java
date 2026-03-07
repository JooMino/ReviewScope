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


    // 분석 요청 처리 (analy.html에서 폼 전송)
    @PostMapping("/search")
    public String search(
            @RequestParam("keyword") String keyword,
            Model model
    ) {
        // 사이트 선택은 화면에서 없앴으니 기본값으로 전체
        String[] sites = new String[]{"dc", "clien", "fmk", "quasar"};

        crawlQueue.add(keyword, sites);

        model.addAttribute("keyword", keyword);
        return "waiting"; // waiting.html: "분석 중입니다..." 화면
    }

    // 결과 페이지 (resul.html)
    @GetMapping("/result")
    public String result(@RequestParam("keyword") String keyword, Model model) {
        CrawlJob job = crawlQueue.get(keyword);

        if (job == null || job.getStatus() != CrawlJob.Status.DONE) {
            model.addAttribute("keyword", keyword);
            return "waiting";
        }

        String filePath = "data_storage/" + keyword + "/" + keyword + "_report.md";
        File file = new File(filePath);
        if (!file.exists()) {
            model.addAttribute("error", "리포트 파일이 없습니다.");
            return "analy";
        }

        Map<String, String> reportData = MdReportParser.parseReport(filePath);

        model.addAttribute("keyword", keyword);
        model.addAttribute("pros", reportData.getOrDefault("pros", "").lines().toList());
        model.addAttribute("cons", reportData.getOrDefault("cons", "").lines().toList());
        model.addAttribute("summary", reportData.getOrDefault("summary", "내용 없음"));

        // 결과를 별도 result.html 대신 analy 오른쪽 패널에 표시
        return "analy";
    }
}
