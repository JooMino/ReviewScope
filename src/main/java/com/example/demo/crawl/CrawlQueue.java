package com.example.demo.crawl;

import org.springframework.stereotype.Component;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class CrawlQueue {
    private final Queue<CrawlJob> queue = new ConcurrentLinkedQueue<>();
    private final ConcurrentHashMap<String, CrawlJob> jobMap = new ConcurrentHashMap<>();

    public void add(String keyword, String[] sites) {
        CrawlJob job = new CrawlJob(keyword, sites);
        job.setStatus(CrawlJob.Status.PENDING); 
        jobMap.put(keyword, job);
        queue.add(job); // ⬅ 이 줄이 반드시 있어야 Python이 poll()로 가져갈 수 있습니다.
    }
    
    public CrawlJob poll() {
        CrawlJob job = queue.poll();
        if (job != null) {
            job.setStatus(CrawlJob.Status.RUNNING);
        }
        return job;
    }

    public CrawlJob get(String keyword) {
        return jobMap.get(keyword);
    }

    public void markDone(String keyword) {
        CrawlJob job = jobMap.get(keyword);
        if (job != null) {
            job.setStatus(CrawlJob.Status.DONE);
        }
    }
}
