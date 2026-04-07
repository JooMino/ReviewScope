package com.example.demo.controller;

import com.example.demo.crawl.CrawlJob;
import com.example.demo.crawl.CrawlQueue;
import com.example.demo.domain.CrawlReport;
import com.example.demo.domain.SourceMap;
import com.example.demo.repository.CrawlReportRepository;
import com.example.demo.repository.SourceMapRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class SearchController {

    private final CrawlQueue crawlQueue;
    private final CrawlReportRepository crawlReportRepository;
    private final SourceMapRepository sourceMapRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SearchController(
            CrawlQueue crawlQueue,
            CrawlReportRepository crawlReportRepository,
            SourceMapRepository sourceMapRepository
    ) {
        this.crawlQueue = crawlQueue;
        this.crawlReportRepository = crawlReportRepository;
        this.sourceMapRepository = sourceMapRepository;
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

        CrawlJob job = crawlQueue.get(keyword);

        if (job != null &&
                job.getStatus() != CrawlJob.Status.DONE &&
                job.getStatus() != CrawlJob.Status.SKIPPED) {

            return Map.of("error", "NOT_READY");
        }

        Optional<CrawlReport> optionalReport = crawlReportRepository.findByKeyword(keyword);
        if (optionalReport.isEmpty()) {
            return Map.of("error", "NO_REPORT");
        }

        CrawlReport report = optionalReport.get();
        String reportContent = report.getReportContent();

        if (reportContent == null || reportContent.trim().isEmpty()) {
            return Map.of("error", "EMPTY_REPORT");
        }

        try {
            JsonNode root = objectMapper.readTree(reportContent);

            // ---------------------------
            // 요약
            // ---------------------------
            String summary = root.path("summary").asText();
            if (summary == null || summary.isBlank()) {
                summary = keyword;
            }

            String positiveSummary = root.path("positive").path("synthesis").asText("내용 없음");
            String negativeSummary = root.path("negative").path("synthesis").asText("내용 없음");

            // ---------------------------
            // 장점 / 단점
            // ---------------------------
            List<Map<String, Object>> pros = buildKeyPointsWithSources(
                    keyword,
                    root.path("positive").path("key_points")
            );

            List<Map<String, Object>> cons = buildKeyPointsWithSources(
                    keyword,
                    root.path("negative").path("key_points")
            );

            // ---------------------------
            // 함께 언급된 모델
            // ---------------------------
            List<String> models = new ArrayList<>();
            for (JsonNode node : root.path("other_products")) {
                String name = node.path("official_name").asText();
                if (name == null || name.isBlank()) {
                    name = node.path("product_name").asText();
                }

                String context = node.path("context").asText();

                if (!name.isBlank() && !context.isBlank()) {
                    models.add(name + " - " + context);
                } else if (!name.isBlank()) {
                    models.add(name);
                }
            }

            // ---------------------------
            // 🔥 핵심: stats 계산 (files 기반)
            // ---------------------------
            Set<String> positiveFiles = new HashSet<>();
            Set<String> negativeFiles = new HashSet<>();

            for (JsonNode node : root.path("positive").path("key_points")) {
                String files = node.path("files").asText("");
                for (String f : files.split(",")) {
                    if (!f.trim().isEmpty()) positiveFiles.add(f.trim());
                }
            }

            for (JsonNode node : root.path("negative").path("key_points")) {
                String files = node.path("files").asText("");
                for (String f : files.split(",")) {
                    if (!f.trim().isEmpty()) negativeFiles.add(f.trim());
                }
            }

            int positiveCount = positiveFiles.size();
            int negativeCount = negativeFiles.size();
            int total = positiveCount + negativeCount;

            Map<String, Object> stats = new HashMap<>();
            stats.put("positiveCount", positiveCount);
            stats.put("negativeCount", negativeCount);
            stats.put("mentionCount", total);

            // ---------------------------
            // 결과 반환
            // ---------------------------
            Map<String, Object> result = new HashMap<>();
            result.put("summary", summary);
            result.put("positiveSummary", positiveSummary);
            result.put("negativeSummary", negativeSummary);
            result.put("pros", pros);
            result.put("cons", cons);
            result.put("models", models);
            result.put("stats", stats);

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", "JSON_PARSE_FAILED");
        }
    }

    // ---------------------------
    // key_points + source 매핑
    // ---------------------------
    private List<Map<String, Object>> buildKeyPointsWithSources(String keyword, JsonNode keyPointsNode) {
        List<Map<String, Object>> results = new ArrayList<>();

        for (JsonNode node : keyPointsNode) {
            String point = node.path("point").asText();
            String dates = node.path("dates").asText();
            String filesRaw = node.path("files").asText();

            List<String> fileNames = Arrays.stream(filesRaw.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .toList();

            List<String> hashes = fileNames.stream()
                    .map(this::extractHashFromFileName)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();

            Map<String, String> hashToUrl = sourceMapRepository
                    .findByKeywordAndHashValueIn(keyword, hashes)
                    .stream()
                    .collect(Collectors.toMap(
                            SourceMap::getHashValue,
                            SourceMap::getUrl,
                            (a, b) -> a,
                            LinkedHashMap::new
                    ));

            List<Map<String, String>> sources = new ArrayList<>();

            for (String fileName : fileNames) {
                String hash = extractHashFromFileName(fileName);
                if (hash == null) continue;

                String url = hashToUrl.get(hash);
                if (url == null) continue;

                Map<String, String> source = new LinkedHashMap<>();
                source.put("file", fileName);
                source.put("hash", hash);
                source.put("url", url);

                sources.add(source);
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("point", point);
            item.put("dates", dates);
            item.put("files", fileNames);
            item.put("sources", sources);

            results.add(item);
        }

        return results;
    }

    private String extractHashFromFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) return null;

        int underscore = fileName.lastIndexOf('_');
        int dot = fileName.lastIndexOf('.');

        if (underscore < 0 || dot < 0 || underscore >= dot) return null;

        return fileName.substring(underscore + 1, dot).trim();
    }
}