package com.example.demo.controller;

public class CrawlResultRequest {
    private String keyword;
    private String status;
    private String reportContent;
    
    // Constructors
    public CrawlResultRequest() {}
    
    // Getters/Setters
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReportContent() { return reportContent; }
    public void setReportContent(String reportContent) { this.reportContent = reportContent; }
}
