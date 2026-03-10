package com.example.demo.crawl;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "crawl_reports")
public class CrawlReport {
    @Id
    private String keyword;
    
    @Column(columnDefinition = "TEXT")
    private String reportContent;
    
    @Enumerated(EnumType.STRING)
    private CrawlJob.Status status;
    
    private LocalDateTime createdAt;
    
    public CrawlReport() {}
    
    public CrawlReport(String keyword, String reportContent, CrawlJob.Status status) {
        this.keyword = keyword;
        this.reportContent = reportContent;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters/Setters
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public String getReportContent() { return reportContent; }
    public void setReportContent(String reportContent) { this.reportContent = reportContent; }
    public CrawlJob.Status getStatus() { return status; }
    public void setStatus(CrawlJob.Status status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}