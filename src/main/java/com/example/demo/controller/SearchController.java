package com.example.demo.controller;

import com.example.demo.crawl.CrawlQueue;
import com.example.demo.crawl.CrawlJob;
import com.example.demo.crawl.CrawlReportRepository;
import com.example.demo.util.MdReportParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController {

    private final CrawlQueue crawlQueue;
    private final CrawlReportRepository crawlReportRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SearchController(CrawlQueue crawlQueue, CrawlReportRepository crawlReportRepository) {
        this.crawlQueue = crawlQueue;
        this.crawlReportRepository = crawlReportRepository;
    }

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/analyze")
    public String analyzePage() {
        return "analy";
    }

    @PostMapping("/search")
    @ResponseBody
    public Map<String, String> search(@RequestParam("keyword") String keyword) {
        crawlQueue.clearQueue();

        String[] sites = new String[]{"dc", "clien", "fmk", "quasar"};
        crawlQueue.add(keyword, sites);
        return Map.of("keyword", keyword, "status", "START");
    }

    @GetMapping("/api/result-data")
    @ResponseBody
    public Map<String, Object> getResultData(@RequestParam("keyword") String keyword) {
        CrawlJob job = crawlQueue.get(keyword);
        if (job == null || job.getStatus() != CrawlJob.Status.DONE) {
            return Map.of("error", "NOT_READY");
        }

        return crawlReportRepository.findByKeyword(keyword)
            .map(report -> {
                String reportContent = report.getReportContent();

                try {
                    JsonNode root = objectMapper.readTree(reportContent);

                    String positiveSummary = root.path("positive").path("synthesis").asText();
                    String negativeSummary = root.path("negative").path("synthesis").asText();

                    List<String> pros = new ArrayList<>();
                    for (JsonNode node : root.path("positive").path("key_points")) {
                        pros.add(node.path("point").asText());
                    }

                    List<String> cons = new ArrayList<>();
                    for (JsonNode node : root.path("negative").path("key_points")) {
                        cons.add(node.path("point").asText());
                    }

                    List<String> models = new ArrayList<>();
                    for (JsonNode node : root.path("other_products")) {
                        String name = node.path("product_name").asText();
                        String context = node.path("context").asText();
                        models.add(name + " - " + context);
                    }

                    return Map.of(
                    	"summary", root.path("keyword").asText(),
                        "positiveSummary", positiveSummary,
                        "negativeSummary", negativeSummary,
                        "pros", pros,
                        "cons", cons,
                        "models", models
                    );
                } catch (Exception e) {
                    // 혹시 예전 md 데이터면 fallback
                    Map<String, String> reportData = MdReportParser.parseReport("data_storage/" + keyword + "/" + keyword + "_report.md");
                    return Map.of(
                        "summary", reportData.getOrDefault("summary", "내용 없음"),
                        "pros", reportData.getOrDefault("pros", "").lines().toList(),
                        "cons", reportData.getOrDefault("cons", "").lines().toList(),
                        "models", reportData.getOrDefault("models", "").lines().toList()
                    );
                }
            })
            .orElse(Map.of("error", "NO_REPORT"));
    }
}