package com.example.demo.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "crawl_job")
public class CrawlJobEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 검색어
    @Column(nullable = false)
    private String keyword;

    // 요청 시간
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // 상태 (PENDING/RUNNING/DONE/FAILED)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public enum Status {
        PENDING, RUNNING, DONE, FAILED
    }

    // 나중에 User 연동할 때 ManyToOne 추가 예정
    // @ManyToOne
    // private User user;

    // getter/setter
}
