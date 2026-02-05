package com.example.demo.crawl;

public class CrawlJob {

    public enum Status {
        PENDING, RUNNING, DONE
    }

    private final String keyword;
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
    private String[] sites;

    public String[] getSites() {
        return sites;
    }

}
