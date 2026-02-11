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
            
            // 1. 전체 내용 저장 (디버깅용)
            sections.put("summary", content); // 일단 전체를 summary에 넣어둠 (못 찾을 경우 대비)
            
            // 2. 패턴별 추출
            // (1) 장점: "- **장점**:" 부터 다음 불릿(-) 전까지
            sections.put("pros", extractListSection(content, "장점"));
            
            // (2) 단점: "- **단점**:" 부터 다음 불릿(-) 전까지
            sections.put("cons", extractListSection(content, "단점"));
            
            // (3) 모델: "함께 언급된 모델" 섹션 (이건 헤더일 수도 있음)
            // 헤더 방식(####)과 리스트 방식 둘 다 시도
            String models = extractSectionByHeader(content, "함께 언급된 모델");
            if (models.isEmpty()) {
                models = extractListSection(content, "함께 언급된 모델");
            }
            sections.put("models", models);

        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return sections;
    }

    // [버전 2] 리스트 아이템 추출기 (- **키워드**: 내용...)
    private static String extractListSection(String content, String keyword) {
        // 정규식 설명:
        // -\s*\*\*{keyword}\*\*:\s*  -> "- **키워드**:" 패턴 찾기 (공백 유연하게)
        // ([\s\S]*?)                 -> 내용 캡처
        // (?=\n\s*-\s*\*\*|$)        -> 다음 "- **..." 패턴이 나오거나 파일 끝까지
        
        String regex = "-\\s*\\*\\*" + keyword + "\\*\\*:\\s*([\\s\\S]*?)(?=\\n\\s*-\\s*\\*\\*|$)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return ""; // 못 찾음
    }
    
    // [버전 1] 헤더 추출기 (### 키워드) - 기존 로직
    private static String extractSectionByHeader(String content, String keyword) {
        String regex = "(?m)^#{1,4}\\s.*?" + keyword + ".*\\n([\\s\\S]*?)(?=(^#{1,4}\\s)|$)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "";
    }
}
