// CsvReader.java (업그레이드 버전)
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
            br.readLine(); // 헤더 건너뛰기
            
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",", -1); 
                
                // [0]file, [1]chars, [2]type, [3]model, [4]summary
                if (data.length > 4) { 
                    // summary 내용 복구 (콤마 때문에 잘린 거 합치기)
                    StringBuilder sb = new StringBuilder();
                    for(int i=4; i<data.length; i++) {
                        sb.append(data[i]);
                        if(i < data.length-1) sb.append(",");
                    }
                    String fullSummary = sb.toString(); // "긍정: 블라블라, 부정: 솰라솰라..."

                    // ★ 여기서 내용을 쪼개서 리뷰 객체를 여러 개 만듭니다! ★
                    
                    // 1. 긍정 내용이 있으면 -> 긍정 리뷰로 추가
                    String positiveContent = extractContent(fullSummary, "긍정:", "부정:");
                    if (!positiveContent.isEmpty()) {
                        reviews.add(createReview(data[0], data[3], positiveContent, "긍정"));
                    }
                    
                    // 2. 부정 내용이 있으면 -> 부정 리뷰로 추가
                    String negativeContent = extractContent(fullSummary, "부정:", "중립"); // '중립' 전까지
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

    // 헬퍼 함수: 텍스트 사이 내용 뽑아내기
    private static String extractContent(String text, String startMarker, String endMarker) {
        int start = text.indexOf(startMarker);
        if (start == -1) return ""; // 시작 키워드 없으면 빈 문자열
        
        start += startMarker.length();
        int end = (endMarker == null) ? -1 : text.indexOf(endMarker, start);
        
        if (end == -1) {
            // 끝 마커가 없으면(혹은 못 찾으면) 끝까지 다 가져옴 (단, 콤마 등 뒤에 잡다한 거 있으면 잘라야 함)
            return text.substring(start).trim();
        } else {
            return text.substring(start, end).trim();
        }
    }

    // 헬퍼 함수: ReviewDto 생성
    private static ReviewDto createReview(String title, String link, String summary, String sentiment) {
        ReviewDto dto = new ReviewDto();
        dto.setTitle(title); // 예: clien
        dto.setLink("#");    // 링크는 없으니 샵
        dto.setSummary(summary);
        dto.setSentiment(sentiment);
        return dto;
    }
}
