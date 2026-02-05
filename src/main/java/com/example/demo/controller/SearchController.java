package com.example.demo.controller;

import com.example.demo.crawl.CrawlQueue;
import com.example.demo.crawl.CrawlJob;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;         


@Controller
public class SearchController {

    private final CrawlQueue crawlQueue;

    public SearchController(CrawlQueue crawlQueue) {
        this.crawlQueue = crawlQueue;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/search")
    public String search(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "sites", required = false) String[] sites,
            Model model
    ) {
        if (sites == null || sites.length == 0) {
            sites = new String[]{"dc", "clien", "fmk", "quasar"};
        }

        crawlQueue.add(keyword, sites);

        model.addAttribute("keyword", keyword);
        return "waiting";   // ❗ result 아님
    }
    
    @GetMapping("/result")
    public String result(
            @RequestParam("keyword") String keyword,
            Model model
    ) {
        CrawlJob job = crawlQueue.get(keyword);

        // 🔴 아직 작업이 없거나 / 끝나지 않았으면
		if (job == null || job.getStatus() != CrawlJob.Status.DONE) {
            model.addAttribute("keyword", keyword);
            return "waiting";   // ⬅ 다시 대기 화면
        }

        // ✅ 여기부터는 "진짜 완료된 경우"
        model.addAttribute("keyword", keyword);

        // TODO: 여기서 txt 결과 읽어서 recentFiles 세팅
        return "result";
    }
}
