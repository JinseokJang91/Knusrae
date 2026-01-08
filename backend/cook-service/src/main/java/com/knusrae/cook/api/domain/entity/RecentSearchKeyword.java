package com.knusrae.cook.api.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 최근 검색어 엔티티
 * 사용자별 최근 검색어를 저장합니다.
 */
@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "recent_search_keyword", indexes = {
    @Index(name = "idx_member_created", columnList = "member_id, created_at DESC")
})
public class RecentSearchKeyword {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, name = "member_id")
    private Long memberId;
    
    @Column(nullable = false, length = 200)
    private String keyword;
    
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

