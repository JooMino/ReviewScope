package com.example.demo.crawl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CrawlReportRepository extends JpaRepository<CrawlReport, String> {
    Optional<CrawlReport> findByKeyword(String keyword);
    
    @Query("SELECT r FROM CrawlReport r WHERE r.keyword = :keyword AND r.createdAt > :sevenDaysAgo")
    Optional<CrawlReport> findRecentByKeyword(@Param("keyword") String keyword, 
                                            @Param("sevenDaysAgo") LocalDateTime sevenDaysAgo);
}
