// src/main/java/com/example/demo/util/CsvReader.java
package com.example.demo.util;

import com.example.demo.util.ReviewDto; // DTO 필요 (아래 참고)
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
                String[] data = line.split(",", -1); // 콤마로 분리
                if (data.length > 5) { // 데이터가 충분하다면
                    ReviewDto review = new ReviewDto();
                    review.setTitle(data[1]);
                    review.setLink(data[2]);
                    review.setSummary(data[3]);
                    review.setSentiment(data[4]); // 긍정/부정
                    reviews.add(review);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return reviews;
    }
}
