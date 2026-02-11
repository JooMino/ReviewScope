package com.example.demo.controller; 

import com.example.demo.util.ReviewDto;
import java.util.List;

public class CrawlResultRequest {
    private String keyword;
    private List<ReviewDto> results; 

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public List<ReviewDto> getResults() { return results; }
    public void setResults(List<ReviewDto> results) { this.results = results; }
}
