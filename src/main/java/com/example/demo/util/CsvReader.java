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
            StringBuilder currentContent = new StringBuilder();
            String currentType = null; // "긍정" or "부정"
            
            // 첫 줄(헤더)은 건너뛰기 (단, 헤더가 확실히 있는지 확인 필요)
            // 만약 첫 줄부터 데이터라면 아래 br.readLine() 지우세요.
            br.readLine(); 

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // 1. "긍정:" 시작 라인 발견
                if (line.startsWith("긍정:") || line.startsWith("긍정 :")) {
                    // 이전에 담고 있던 게 있으면 저장
                    saveReview(reviews, currentType, currentContent);
                    
                    // 새 긍정 시작
                    currentType = "긍정";
                    currentContent.setLength(0); // 버퍼 초기화
                    currentContent.append(removePrefix(line, "긍정"));
                }
                // 2. "부정:" 시작 라인 발견
                else if (line.startsWith("부정:") || line.startsWith("부정 :")) {
                    // 이전에 담고 있던 게 있으면 저장
                    saveReview(reviews, currentType, currentContent);
                    
                    // 새 부정 시작
                    currentType = "부정";
                    currentContent.setLength(0);
                    currentContent.append(removePrefix(line, "부정"));
                }
                // 3. "중립"이나 "질문"이 나오면 -> 이전 내용 저장하고 끊기
                else if (line.startsWith("중립") || line.startsWith("질문")) {
                    saveReview(reviews, currentType, currentContent);
                    currentType = null; // 중립은 저장 안 함 (필요하면 여기서 "중립" 처리)
                }
                // 4. 그냥 내용이 이어지는 줄 (멀티라인)
                else {
                    if (currentType != null) {
                        currentContent.append("\n").append(line);
                    }
                }
            }
            // 마지막에 남은 데이터 저장
            saveReview(reviews, currentType, currentContent);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return reviews;
    }

    // 리뷰 리스트에 추가하는 함수
    private static void saveReview(List<ReviewDto> reviews, String type, StringBuilder content) {
        if (type != null && content.length() > 0) {
            ReviewDto dto = new ReviewDto();
            dto.setTitle("분석결과"); // 제목은 고정 (CSV에 제목이 따로 없다면)
            dto.setLink("#");
            dto.setSummary(content.toString().trim());
            dto.setSentiment(type);
            reviews.add(dto);
        }
    }

    // "긍정:" 같은 앞부분 떼어내는 함수
    private static String removePrefix(String text, String prefix) {
        int idx = text.indexOf(prefix);
        if (idx == -1) return text;
        
        String clean = text.substring(idx + prefix.length());
        if (clean.startsWith(":") || clean.startsWith(" :")) {
            clean = clean.replaceFirst("^[:\\s]+", "");
        }
        return clean.trim();
    }
}
