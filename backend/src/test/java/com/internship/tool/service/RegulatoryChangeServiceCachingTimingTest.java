package com.internship.tool.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.internship.tool.dto.RegulatoryChangeResponse;
import com.internship.tool.entity.RegulatoryChange;
import com.internship.tool.repository.RegulatoryChangeRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootTest(classes = {RegulatoryChangeServiceImpl.class, RegulatoryChangeServiceCachingTimingTest.CacheTestConfig.class})
@Import(RegulatoryChangeServiceCachingTimingTest.CacheTestConfig.class)
class RegulatoryChangeServiceCachingTimingTest {

    @TestConfiguration
    @EnableCaching
    static class CacheTestConfig {

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("regulatoryChangesById", "regulatoryChangesAll");
        }
    }

    @Autowired
    private RegulatoryChangeService regulatoryChangeService;

    @MockBean
    private RegulatoryChangeRepository regulatoryChangeRepository;

    @MockBean
    private NotificationEmailService notificationEmailService;

    @BeforeEach
    void setUp() {
        RegulatoryChange entity = new RegulatoryChange();
        entity.setId(1L);
        entity.setTitle("RBI Circular");
        entity.setDescription("Updated KYC norms");
        entity.setSource("RBI");
        entity.setJurisdiction("India");
        entity.setCategory("Compliance");
        entity.setStatus("NEW");
        entity.setPriority("HIGH");
        entity.setPublishedDate(LocalDate.now().minusDays(1));
        entity.setEffectiveDate(LocalDate.now().plusDays(10));
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        when(regulatoryChangeRepository.findById(1L)).thenAnswer(invocation -> {
            Thread.sleep(180);
            return Optional.of(entity);
        });
    }

    @Test
    void getById_shouldBeFasterOnSecondCall_dueToCacheHit() {
        long missStart = System.nanoTime();
        RegulatoryChangeResponse missResponse = regulatoryChangeService.getById(1L);
        long missDurationNs = System.nanoTime() - missStart;

        long hitStart = System.nanoTime();
        RegulatoryChangeResponse hitResponse = regulatoryChangeService.getById(1L);
        long hitDurationNs = System.nanoTime() - hitStart;

        long missDurationMs = missDurationNs / 1_000_000;
        long hitDurationMs = hitDurationNs / 1_000_000;

        System.out.println("Cache miss duration (ms): " + missDurationMs);
        System.out.println("Cache hit duration  (ms): " + hitDurationMs);

        assertTrue(missDurationMs > hitDurationMs, "Expected cache hit to be faster than miss");
        assertTrue(missResponse.getId().equals(hitResponse.getId()));
        verify(regulatoryChangeRepository, times(1)).findById(1L);
    }
}
