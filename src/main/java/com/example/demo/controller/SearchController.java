package com.example.demo.controller;

import com.example.demo.crawl.CrawlJob;
import com.example.demo.crawl.CrawlQueue;
import com.example.demo.crawl.CrawlReport;
import com.example.demo.crawl.CrawlReportRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    @GetMapping("/analyze/result")
    public String analyzeResultPage(@RequestParam("keyword") String keyword, Model model) {
        model.addAttribute("keyword", keyword);
        return "analy-result";
    }
    @PostMapping("/search")
    @ResponseBody
    public Map<String, String> search(@RequestParam("keyword") String keyword) {
        crawlQueue.clearQueue();

        String[] sites = new String[]{"dc", "clien", "fmk", "quasar"};
        crawlQueue.add(keyword, sites);

        return Map.of(
            "keyword", keyword,
            "status", "START"
        );
    }

    @GetMapping("/api/result-data")
    @ResponseBody
    public Map<String, Object> getResultData(@RequestParam("keyword") String keyword) {
        Optional<CrawlReport> optionalReport = crawlReportRepository.findByKeyword(keyword);
        if (optionalReport.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "NO_REPORT");
            return error;
        }

        CrawlReport report = optionalReport.get();

        if (report.getStatus() != CrawlJob.Status.DONE) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "NOT_READY");
            return error;
        }

        String reportContent = report.getReportContent();
        if (reportContent == null || reportContent.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "EMPTY_REPORT");
            return error;
        }

        try {
            JsonNode root = objectMapper.readTree(reportContent);

            String summary = root.path("summary").asText();
            if (summary == null || summary.trim().isEmpty()) {
                summary = keyword;
            }

            String positiveSummary = root.path("positive").path("synthesis").asText();
            if (positiveSummary == null || positiveSummary.trim().isEmpty()) {
                positiveSummary = "내용 없음";
            }

            String negativeSummary = root.path("negative").path("synthesis").asText();
            if (negativeSummary == null || negativeSummary.trim().isEmpty()) {
                negativeSummary = "내용 없음";
            }

            List<String> pros = new ArrayList<>();
            for (JsonNode node : root.path("positive").path("key_points")) {
                String point = node.path("point").asText();
                if (point != null && !point.trim().isEmpty()) {
                    pros.add(point);
                }
            }

            List<String> cons = new ArrayList<>();
            for (JsonNode node : root.path("negative").path("key_points")) {
                String point = node.path("point").asText();
                if (point != null && !point.trim().isEmpty()) {
                    cons.add(point);
                }
            }

            List<String> models = new ArrayList<>();
            for (JsonNode node : root.path("other_products")) {
                String name = node.path("product_name").asText();
                String context = node.path("context").asText();

                boolean hasName = name != null && !name.trim().isEmpty();
                boolean hasContext = context != null && !context.trim().isEmpty();

                if (hasName && hasContext) {
                    models.add(name + " - " + context);
                } else if (hasName) {
                    models.add(name);
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("summary", summary);
            result.put("positiveSummary", positiveSummary);
            result.put("negativeSummary", negativeSummary);
            result.put("pros", pros);
            result.put("cons", cons);
            result.put("models", models);

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("error", "JSON_PARSE_FAILED");
            return error;
        }
    }
}