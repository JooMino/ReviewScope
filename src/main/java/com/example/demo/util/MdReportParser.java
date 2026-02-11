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
            // 1. 파일 읽기
            String content = Files.readString(Path.of(filePath));
            
            // 2. 섹션별 파싱 (정규표현식 활용)
            // (1) 장점 (pros) 찾기
            // "장점" 또는 "Pros" 라는 단어가 포함된 헤더부터 ~ 다음 헤더 전까지
            sections.put("pros", extractSection(content, "(장점|Pros)"));

            // (2) 단점 (cons) 찾기
            sections.put("cons", extractSection(content, "(단점|Cons)"));

            // (3) 함께 언급된 모델 (models)
            sections.put("models", extractSection(content, "(함께 언급된 모델|Related Models)"));

            // (4) 전체 내용 (fallback용)
            sections.put("fullContent", content);

        } catch (IOException e) {
            e.printStackTrace();
            sections.put("error", "파일을 읽을 수 없습니다.");
        }
        
        return sections;
    }

    // 특정 키워드가 포함된 헤더(### 키워드)부터 다음 헤더(#) 전까지 추출하는 메서드
    private static String extractSection(String content, String keywordPattern) {
        // 정규식 설명:
        // ###.*?패턴.*?\n  -> '###'으로 시작하고 패턴을 포함하는 줄 찾기
        // ([\s\S]*?)       -> 그 아래 내용 캡처 (줄바꿈 포함)
        // (?=###|$)        -> 다음 '###' 헤더가 나오거나 파일 끝($)이 나올 때까지
        
        String regex = "(?m)^#{1,3}\\s.*?" + keywordPattern + ".*\\n([\\s\\S]*?)(?=(^#{1,3}\\s)|$)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            return matcher.group(1).trim(); // 내용만 반환
        }
        return ""; // 못 찾으면 빈 문자열
    }
}
