package com.knusrae.cook.api.theme.domain.repository;

import com.knusrae.cook.api.theme.domain.entity.ThemeCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ThemeCollectionRepository extends JpaRepository<ThemeCollection, Long> {

    List<ThemeCollection> findByStatusOrderBySortOrderAsc(String status);

    @Query("""
        SELECT tc FROM ThemeCollection tc
        WHERE tc.status = 'ACTIVE'
        AND (tc.startDate IS NULL OR tc.startDate <= :now)
        AND (tc.endDate IS NULL OR tc.endDate >= :now)
        ORDER BY tc.sortOrder ASC
        """)
    List<ThemeCollection> findActiveThemesNow(LocalDate now);

    @Query("""
        SELECT tc FROM ThemeCollection tc
        WHERE tc.status = 'ACTIVE'
        AND tc.endDate IS NOT NULL
        AND tc.endDate < :now
        """)
    List<ThemeCollection> findExpiredThemes(LocalDate now);
}
