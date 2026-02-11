package com.example.demo.controller; 

// ReviewDto import 제거

public class CrawlResultRequest {
    private String keyword;
    
    // results 필드 삭제함 (파이썬에서도 results: [] 보내지만, 여기서 안 받으면 무시됨. OK)
    
    // 핵심 필드들
    private String reportContent; // MD 내용
    private String status;        // SUCCESS / FAIL
    private String errorMessage;

    // Getter & Setter
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public String getReportContent() { return reportContent; }
    public void setReportContent(String reportContent) { this.reportContent = reportContent; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}