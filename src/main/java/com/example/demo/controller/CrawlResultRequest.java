package com.example.demo.controller; // 패키지 확인

import com.example.demo.util.ReviewDto;
import java.util.List;

public class CrawlResultRequest {
    private String keyword;
    private List<ReviewDto> results; // 파이썬에서 보낼 JSON 키 이름과 같아야 함

    // Getter & Setter (필수!)
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public List<ReviewDto> getResults() { return results; }
    public void setResults(List<ReviewDto> results) { this.results = results; }
}
