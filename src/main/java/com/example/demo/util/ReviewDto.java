package com.example.demo.util;

public class ReviewDto {
    private String title;      // 제목
    private String link;       // 링크
    private String summary;    // 요약
    private String sentiment;  // 긍정/부정

    // 기본 생성자
    public ReviewDto() {}

    // Getter & Setter 
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getSentiment() { return sentiment; }
    public void setSentiment(String sentiment) { this.sentiment = sentiment; }
}
