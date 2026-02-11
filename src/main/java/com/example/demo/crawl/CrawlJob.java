package com.example.demo.crawl;



public class CrawlJob {
    private String keyword;
    private String[] sites;
 
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

}
