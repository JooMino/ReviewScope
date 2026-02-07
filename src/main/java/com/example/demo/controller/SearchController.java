package com.example.demo.controller;

import com.example.demo.crawl.CrawlQueue;
import com.example.demo.crawl.CrawlJob;
import com.example.demo.util.ReviewDto;
import com.example.demo.util.CsvReader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        // 사이트 선택이 없으면 기본값 설정 (디시, 클리앙, 펨코, 퀘이사존)
        if (sites == null || sites.length == 0) {
            sites = new String[]{"dc", "clien", "fmk", "quasar"};
        }

        // 큐에 작업 등록 (나중에 Python이 가져감)
        crawlQueue.add(keyword, sites);

        model.addAttribute("keyword", keyword);
        return "waiting";   // 대기 화면으로 이동
    }
    
    // 결과 페이지 (대기 화면에서 주기적으로 호출하거나 새로고침 시)
    @GetMapping("/result")
    public String result(
            @RequestParam("keyword") String keyword,
            Model model
    ) {
        CrawlJob job = crawlQueue.get(keyword);

        // 1. 작업이 없거나 아직 진행 중이면 계속 대기
        if (job == null || job.getStatus() != CrawlJob.Status.DONE) {
            model.addAttribute("keyword", keyword);
            return "waiting";
        }

        // 2. 작업 완료됨! (Status.DONE)
        // Python이 저장한 CSV 파일 경로 (Python의 저장 로직과 맞춰야 함)
        // 예: 프로젝트 루트 기준 data_storage/키워드/result.csv 라고 가정
        // ⚠️ 실제 파일 경로가 다르면 수정 필요!
        String filePath = "data_storage/" + keyword + "/result.csv"; 
        // 만약 파일이 없다면 예외처리나 빈 리스트가 반환됨
        
        List<ReviewDto> allReviews = CsvReader.readReviews(filePath);
        if (allReviews == null) {
            allReviews = new ArrayList<>();
        }

        // 3. 긍정 리뷰 3개 추출
        List<ReviewDto> positiveReviews = allReviews.stream()
                .filter(r -> "긍정".equals(r.getSentiment()))
                .limit(3)
                .collect(Collectors.toList());

        // 4. 부정 리뷰 3개 추출
        List<ReviewDto> negativeReviews = allReviews.stream()
                .filter(r -> "부정".equals(r.getSentiment()))
                .limit(3)
                .collect(Collectors.toList());

        // 5. 데이터를 모델에 담아서 HTML로 전달
        model.addAttribute("keyword", keyword);
        model.addAttribute("positives", positiveReviews);
        model.addAttribute("negatives", negativeReviews);
        model.addAttribute("totalCount", allReviews.size()); // 전체 개수

        return "result"; // result.html 보여줌
    }
}
