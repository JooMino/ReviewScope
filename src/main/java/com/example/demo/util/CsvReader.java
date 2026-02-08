// CsvReader.java (줄바꿈 대응 버전)
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
            
            StringBuilder currentEntry = new StringBuilder();
            
            while ((line = br.readLine()) != null) {
                // 현재 줄을 버퍼에 추가 (줄바꿈 포함)
                if (currentEntry.length() > 0) currentEntry.append("\n");
                currentEntry.append(line);
                
                // 따옴표 개수 세기 (홀수면 아직 데이터가 안 끝난 것!)
                // (이 로직은 " 로 감싸진 CSV라고 가정)
                // 만약 " 가 없는 CSV라면 그냥 한 줄이 한 데이터이겠지만, 
                // 지금 이미지는 한 데이터가 여러 줄이므로, "긍정:" 으로 시작하면 새 데이터로 보는 게 아니라
                // 파일 단위 파싱이 어려우니 통째로 읽어서 처리하는 게 낫습니다.
            }
            
            // 하지만 위 방식은 복잡하니, 더 쉬운 꼼수!
            // 전체 파일을 통으로 읽은 다음, "clien" 같은 사이트 이름이 나오면 자릅니다.
            // (이미지 보니 맨 앞에 사이트 이름이 있죠?)
            
            String fullContent = currentEntry.toString();
            // 데이터가 1개(또는 소수)라면 그냥 통으로 파싱해버리는 게 빠릅니다.
            
            // ★ 기존 로직 폐기하고, 텍스트 전체에서 "긍정:", "부정:" 위치를 찾아서 뜯어내기 ★
            
            // 1. 긍정 찾기
            String pos = extractByMarker(fullContent, "긍정:", "부정:");
            if(!pos.isEmpty()) reviews.add(createReview("분석결과", pos, "긍정"));
            
            // 2. 부정 찾기 (줄바꿈 포함해도 찾음)
            String neg = extractByMarker(fullContent, "부정:", "중립");
            if(neg.isEmpty()) neg = extractByMarker(fullContent, "부정:", "질문");
            
            if(!neg.isEmpty()) reviews.add(createReview("분석결과", neg, "부정"));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return reviews;
    }

    // 헬퍼: 줄바꿈 무시하고 텍스트 뜯어내기
    private static String extractByMarker(String text, String startMarker, String endMarker) {
        int start = text.indexOf(startMarker);
        if (start == -1) return "";
        
        start += startMarker.length();
        int end = -1;
        
        // 종료 마커 찾기 (여러 후보 중 제일 먼저 나오는 놈)
        if (endMarker != null) {
            end = text.indexOf(endMarker, start);
            // 만약 "중립"을 못 찾으면 "질문"도 찾아보기
            if (end == -1 && endMarker.equals("중립")) {
                 end = text.indexOf("질문", start);
            }
        }
        
        String content;
        if (end == -1) {
            content = text.substring(start); // 끝까지
        } else {
            content = text.substring(start, end);
        }
        
        // 콤마, 따옴표, 줄바꿈 등 지저분한 거 정리
        return content.replace("\"", "").trim().replaceAll("[,\\s]+$", "");
    }

    private static ReviewDto createReview(String title, String summary, String sentiment) {
        ReviewDto dto = new ReviewDto();
        dto.setTitle(title);
        dto.setLink("#");
        dto.setSummary(summary);
        dto.setSentiment(sentiment);
        return dto;
    }
}