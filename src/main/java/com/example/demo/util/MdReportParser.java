package com.example.demo.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class MdReportParser {

    public static Map<String, String> parseReport(String filePath) {
        Map<String, String> sections = new HashMap<>();
        
        try {
            String content = Files.readString(Path.of(filePath));
            
            // 1. 전체 내용
            sections.put("summary", content);
            
            // 2. 단순 문자열 검색으로 추출
            sections.put("pros", extractByKeyword(content, "- **장점**:"));
            sections.put("cons", extractByKeyword(content, "- **단점**:"));
            
            // 모델 섹션은 헤더(####)일 수도 있고 리스트(- **)일 수도 있음 -> 둘 다 시도
            String models = extractByKeyword(content, "#### 함께 언급된 모델");
            if (models.isEmpty()) {
                models = extractByKeyword(content, "- **함께 언급된 모델");
            }
            sections.put("models", models);

        } catch (IOException e) {
            e.printStackTrace();
            sections.put("error", "Error parsing report.");
        }
        
        return sections;
    }

    private static String extractByKeyword(String content, String startKeyword) {
        // 1. 시작 키워드 위치 찾기
        int startIndex = content.indexOf(startKeyword);
        if (startIndex == -1) {
            return ""; // 못 찾음
        }

        // 본문 시작점: 키워드 길이만큼 뒤로 이동 (예: "- **장점**:" 뒤부터 읽어야 하니까)
        int contentStart = startIndex + startKeyword.length();
        
        // 2. 끝점 찾기 (다음 섹션 후보들 중 가장 가까운 것)
        String[] endMarkers = {
            "- **단점**:", 
            "- **함께 언급된 모델", 
            "#### 함께 언급된 모델", 
            "#### QCY", // 다른 헤더들 예시
            "### 2.", 
            "### 3.",
            "## 결론",
            "## 본론" 
        };

        int bestEndIndex = content.length();
        
        for (String marker : endMarkers) {
            // 현재 찾으려는 키워드와 똑같은 건 건너뜀 (장점 찾는데 장점에서 멈추면 안 됨)
            if (marker.equals(startKeyword)) continue;
            
            // contentStart 이후에 나오는 마커를 찾음
            int foundIndex = content.indexOf(marker, contentStart);
            if (foundIndex != -1 && foundIndex < bestEndIndex) {
                bestEndIndex = foundIndex;
            }
        }
        
        // 3. 자르기 & 공백 정리
        if (contentStart >= bestEndIndex) return "";
        
        // 앞뒤 공백 및 줄바꿈 제거
        return content.substring(contentStart, bestEndIndex).trim();
    }
}


