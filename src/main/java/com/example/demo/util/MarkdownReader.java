package com.example.demo.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class MarkdownReader {

    public static List<ReviewDto> readReviews(String filePath) {
        List<ReviewDto> reviews = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            ReviewDto currentDto = null;
            StringBuilder summaryBuffer = new StringBuilder();
            boolean isReadingSummary = false;

            while ((line = br.readLine()) != null) {
                line = line.trim();

                // 1. 구분선(---)을 만나면 이전 DTO 저장 및 초기화
                if (line.equals("---")) {
                    if (currentDto != null) {
                        currentDto.setSummary(summaryBuffer.toString().trim());
                        reviews.add(currentDto);
                        currentDto = null;
                        summaryBuffer.setLength(0); // 버퍼 비우기
                        isReadingSummary = false;
                    }
                    continue;
                }

                // 2. 제목 라인 (### 로 시작) -> 새 객체 시작으로 간주
                if (line.startsWith("### ")) {
                    // 혹시 저장되지 않은 이전 객체가 있다면 저장 (안전장치)
                    if (currentDto != null) {
                        currentDto.setSummary(summaryBuffer.toString().trim());
                        reviews.add(currentDto);
                        summaryBuffer.setLength(0);
                    }
                    
                    currentDto = new ReviewDto();
                    currentDto.setTitle(line.substring(4)); // "### " 제거
                    isReadingSummary = false; 
                }
                // 3. 링크 파싱
                else if (line.startsWith("- **링크**:")) {
                    if (currentDto != null) {
                        currentDto.setLink(removePrefix(line, "- **링크**:"));
                    }
                }
                // 4. 감성 파싱
                else if (line.startsWith("- **감성**:")) {
                    if (currentDto != null) {
                        currentDto.setSentiment(removePrefix(line, "- **감성**:"));
                    }
                }
                // 5. 요약 라벨 발견 -> 다음 줄부터 요약 내용임
                else if (line.equals("- **요약**:")) {
                    isReadingSummary = true;
                }
                // 6. 요약 내용 읽기
                else if (isReadingSummary && !line.isEmpty()) {
                    // 헤더나 메타데이터가 아닌 경우만 내용으로 추가
                    if (!line.startsWith("# ") && !line.startsWith("Generated at") && !line.startsWith("Total items")) {
                         if (summaryBuffer.length() > 0) summaryBuffer.append("\n");
                         summaryBuffer.append(line);
                    }
                }
            }
            
            // 파일 끝: 마지막 객체 저장
            if (currentDto != null) {
                currentDto.setSummary(summaryBuffer.toString().trim());
                reviews.add(currentDto);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return reviews;
    }

    // "접두어" 떼어내는 헬퍼 함수
    private static String removePrefix(String text, String prefix) {
        if (text.startsWith(prefix)) {
            return text.substring(prefix.length()).trim();
        }
        return text;
    }
}
