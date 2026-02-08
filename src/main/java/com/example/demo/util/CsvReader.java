package com.example.demo.util;


import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class CsvReader {

    public static List<ReviewDto> readReviews(String filePath) {
        List<ReviewDto> reviews = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // 첫 줄(헤더) 건너뛰기
            
            while ((line = br.readLine()) != null) {
                // 콤마로 분리 (빈 값도 포함)
                String[] data = line.split(",", -1); 
                
                // 데이터 구조: [0]file, [1]chars, [2]type, [3]model, [4]summary, [5]sentiment(있으면)
                if (data.length > 4) { 
                    
                    // summary 내용 복구 (내용 중에 콤마가 있어서 잘렸을 경우 다시 합침)
                    StringBuilder sb = new StringBuilder();
                    for(int i=4; i<data.length; i++) {
                        // sentiment 컬럼(마지막) 전까지만 summary로 봄 (만약 sentiment가 있다면)
                        // 하지만 지금 CSV 구조상 summary가 끝까지 간다고 가정하고 싹 긁어옴
                        sb.append(data[i]);
                        if(i < data.length-1) sb.append(","); 
                    }
                    String fullSummary = sb.toString(); 

                    // ★ 핵심 수정: 긍정/부정 내용 발라내기 ★
                    
                    // 1. 긍정 내용 추출
                    String positiveContent = extractContent(fullSummary, "긍정");
                    if (!positiveContent.isEmpty()) {
                        reviews.add(createReview(data[0], data[3], positiveContent, "긍정"));
                    }
                    
                    // 2. 부정 내용 추출 (좀 더 유연하게 "부정" 단어 찾기)
                    String negativeContent = extractContent(fullSummary, "부정");
                    if (!negativeContent.isEmpty()) {
                        reviews.add(createReview(data[0], data[3], negativeContent, "부정"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return reviews;
    }

    // 헬퍼 함수: 특정 키워드(marker) 뒤의 내용을 뽑아내는 똑똑한 함수
    private static String extractContent(String text, String marker) {
        // "긍정" 또는 "부정" 위치 찾기
        int startIdx = text.indexOf(marker);
        if (startIdx == -1) return ""; 

        // marker 뒤쪽(예: "긍정:" 다음부터)
        String content = text.substring(startIdx + marker.length());
        
        // 혹시 ":" 나 " " 공백이 있으면 제거
        if (content.startsWith(":")) content = content.substring(1);
        content = content.trim();

        // 다음 섹션("부정"이나 "중립" 등)이 나오면 그 앞까지만 자르기
        int nextSectionIdx = -1;
        
        if (marker.equals("긍정")) {
            // 긍정 내용은 "부정"이나 "중립" 나오기 전까지
            nextSectionIdx = content.indexOf("부정");
            if (nextSectionIdx == -1) nextSectionIdx = content.indexOf("중립");
        } else if (marker.equals("부정")) {
            // 부정 내용은 "중립" 나오기 전까지
            nextSectionIdx = content.indexOf("중립");
             // 혹시 "질문" 같은 게 뒤에 오면 거기서 끊기
            if (nextSectionIdx == -1) nextSectionIdx = content.indexOf("질문");
        }
        
        // 뒤에 다른 섹션이 있으면 거기서 자름
        if (nextSectionIdx != -1) {
            content = content.substring(0, nextSectionIdx);
        }
        
        // 뒤에 남은 콤마나 공백 제거
        return content.replaceAll("[,\\s]+$", "").trim();
    }

    // 헬퍼 함수: ReviewDto 객체 생성
    private static ReviewDto createReview(String title, String link, String summary, String sentiment) {
        ReviewDto dto = new ReviewDto();
        dto.setTitle(title); 
        dto.setLink("#");    
        dto.setSummary(summary);
        dto.setSentiment(sentiment);
        return dto;
    }
}