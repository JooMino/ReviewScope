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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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

        // RUNNING / PENDING / FAILED 일 때만 NOT_READY
        if (job != null &&
            job.getStatus() != CrawlJob.Status.DONE &&
            job.getStatus() != CrawlJob.Status.SKIPPED) {

            Map<String, Object> error = new HashMap<>();
            error.put("error", "NOT_READY");
            return error;
        }

        Optional<CrawlReport> optionalReport = crawlReportRepository.findByKeyword(keyword);
        if (optionalReport.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "NO_REPORT");
            return error;
        }

        CrawlReport report = optionalReport.get();
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
                summary = root.path("keyword").asText();
            }
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

            List<Map<String, Object>> pros = buildKeyPointsWithSources(
                    keyword,
                    root.path("positive").path("key_points")
            );

            List<Map<String, Object>> cons = buildKeyPointsWithSources(
                    keyword,
                    root.path("negative").path("key_points")
            );

            List<String> models = new ArrayList<>();
            for (JsonNode node : root.path("other_products")) {
                String name = node.path("official_name").asText();

                if (name == null || name.trim().isEmpty()) {
                    name = node.path("product_name").asText();
                }

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

    private List<Map<String, Object>> buildKeyPointsWithSources(String keyword, JsonNode keyPointsNode) {
        List<Map<String, Object>> results = new ArrayList<>();

        for (JsonNode node : keyPointsNode) {
            String point = node.path("point").asText();
            String dates = node.path("dates").asText();
            String filesRaw = node.path("files").asText();

            List<String> fileNames = parseFileNames(filesRaw);
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
                if (url == null || url.isBlank()) continue;

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

    private List<String> parseFileNames(String filesRaw) {
        if (filesRaw == null || filesRaw.isBlank()) {
            return List.of();
        }

        return Arrays.stream(filesRaw.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    private String extractHashFromFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return null;
        }

        int underscoreIndex = fileName.lastIndexOf('_');
        int dotIndex = fileName.lastIndexOf('.');

        if (underscoreIndex < 0 || dotIndex < 0 || underscoreIndex >= dotIndex) {
            return null;
        }

        return fileName.substring(underscoreIndex + 1, dotIndex).trim();
    }
}