package com.example.demo.crawl;
import java.util.List;

import com.example.demo.util.ReviewDto;

public class CrawlJob {
    private String keyword;
    private String[] sites;

    private List<ReviewDto> results; 
    public enum Status {
        PENDING, RUNNING, DONE,FAILED
    }

    private Status status = Status.RUNNING; 

    public CrawlJob(String keyword, String[] sites) {
        this.keyword = keyword;
        this.sites = sites;
        this.status = Status.PENDING;
    }
    public String getKeyword() {
        return keyword;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String[] getSites() {
        return sites;
    }
    public List<ReviewDto> getResults() { 
        return results; 
    }
    public void setResults(List<ReviewDto> results) {
        this.results = results; 
    }
}
