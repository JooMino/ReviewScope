package com.example.demo.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MdReportParser {

    public static Map<String, String> parseReport(String filePath) {
        Map<String, String> sections = new HashMap<>();
        
        try {
            String content = Files.readString(Path.of(filePath));
            sections.put("summary", content); // 전체 내용
            
            // ★ 구조 기반 추출
            // 1. 장점: "- **장점**:" (또는 "장점:") 라인부터 ~ 다음 구조(- **...**: 또는 #### ...) 전까지
            sections.put("pros", extractStructSection(content, "장점"));
            
            // 2. 단점: "- **단점**:" (또는 "단점:") 라인부터 ~ 다음 구조 전까지
            sections.put("cons", extractStructSection(content, "단점"));
            
            // 3. 모델: "#### 함께 언급된 모델" (또는 "- **함께...**") 부터 ~ 다음 구조 전까지
            sections.put("models", extractStructSection(content, "함께 언급된 모델"));

        } catch (IOException e) {
            e.printStackTrace();
            sections.put("error", "Error parsing report.");
        }
        
        return sections;
    }

    private static String extractStructSection(String content, String keyword) {
        // [1] 시작 패턴 정의 (엄격하게)
        // - **키워드**:  (가장 흔함)
        // #### 키워드    (모델 섹션 등)
        // ### 키워드
        String startRegex = "(?m)^\\s*(- \\*\\*|#{3,4} )" + keyword + ".*$";
        Pattern startPattern = Pattern.compile(startRegex);
        Matcher startMatcher = startPattern.matcher(content);

        if (!startMatcher.find()) {
            return "정보 없음";
        }

        int contentStart = startMatcher.end(); // 키워드 라인 끝에서 시작

        // [2] 종료 패턴 정의 (다음 섹션의 시작부)
        // 다음 섹션의 특징:
        // 1. 줄 시작이 "- **...**:" 형태 (다른 주요 항목)
        // 2. 줄 시작이 "###..." 또는 "####..." 형태 (헤더)
        // 3. 줄 시작이 "##..." 형태 (큰 헤더)
        String endRegex = "(?m)^\\s*(- \\*\\*.*\\*\\*:|#{2,4} ).*$";
        
        Pattern endPattern = Pattern.compile(endRegex);
        Matcher endMatcher = endPattern.matcher(content);
        
        int bestEndIndex = content.length(); // 못 찾으면 파일 끝까지

        // contentStart 이후에 나오는 첫 번째 '구조적 헤더'를 찾음
        while (endMatcher.find()) {
            if (endMatcher.start() > contentStart) {
                bestEndIndex = endMatcher.start();
                break; // 가장 가까운 다음 헤더를 찾았으면 중단
            }
        }
        
        // [3] 추출 및 다듬기
        if (contentStart >= bestEndIndex) return "";
        
        return content.substring(contentStart, bestEndIndex).trim();
    }
}

