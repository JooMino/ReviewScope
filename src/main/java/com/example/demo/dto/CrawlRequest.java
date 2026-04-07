package com.example.demo.dto;

public class CrawlRequest {

    private String keyword;

    public CrawlRequest(String keyword) {
        this.keyword = keyword;
    }

    public String getKeyword() {
        return keyword;
    }
}
