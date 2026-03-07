package com.example.demo.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "crawl_result")
public class CrawlResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 작업의 결과인지
    @OneToOne
    @JoinColumn(name = "crawl_job_id", nullable = false)
    private CrawlJobEntity crawlJob;

    // MD 파일 경로 (예: data_storage/키워드/키워드_report.md)
    @Column(nullable = false)
    private String reportPath;

    // 나중에 검색 결과 요약, 모델 리스트 등을 컬럼으로 빼고 싶으면 여기에 추가
    // @Column(columnDefinition = "TEXT")
    // private String summary;

    protected CrawlResultEntity() {}

    public CrawlResultEntity(CrawlJobEntity crawlJob, String reportPath) {
        this.crawlJob = crawlJob;
        this.reportPath = reportPath;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public CrawlJobEntity getCrawlJob() {
		return crawlJob;
	}

	public void setCrawlJob(CrawlJobEntity crawlJob) {
		this.crawlJob = crawlJob;
	}

	public String getReportPath() {
		return reportPath;
	}

	public void setReportPath(String reportPath) {
		this.reportPath = reportPath;
	}

}
