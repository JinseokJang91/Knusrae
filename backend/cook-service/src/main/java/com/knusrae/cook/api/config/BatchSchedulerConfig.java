package com.knusrae.cook.api.config;

import com.knusrae.cook.api.domain.service.PopularityCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class BatchSchedulerConfig {
    
    private final PopularityCalculationService popularityCalculationService;
    
    /**
     * 인기도 점수 계산 및 순위 추적 - 매 1시간마다
     * 매시 정각에 실행
     */
    @Scheduled(cron = "0 0 * * * *")
    public void calculatePopularityScores() {
        log.info("Starting scheduled popularity score calculation");
        try {
            // 1. 인기도 점수 계산
            popularityCalculationService.calculateAllPopularityScores();
            log.info("Popularity score calculation completed");
            
            // 2. 순위 변동 추적
            popularityCalculationService.trackRankingChanges();
            log.info("Ranking change tracking completed");
            
            log.info("Scheduled popularity score calculation completed successfully");
        } catch (Exception e) {
            log.error("Error during scheduled popularity score calculation", e);
        }
    }
}

