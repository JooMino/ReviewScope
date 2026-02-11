package com.example.demo.controller; 

import com.example.demo.util.ReviewDto;
import java.util.List;

public class CrawlResultRequest {
    private String keyword;
    private List<ReviewDto> results;
    
    // ★★★ [추가] 실패/성공 상태와 에러 메시지를 받을 변수
    private String status;       
    private String errorMessage; 

    // 기존 Getter/Setter
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public List<ReviewDto> getResults() { return results; }
    public void setResults(List<ReviewDto> results) { this.results = results; }

    // ★★★ [추가] 새로 만든 변수의 Getter/Setter
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
