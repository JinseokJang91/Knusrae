package com.knusrae.cook.api.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "ingredient_request",
       indexes = {
           @Index(name = "idx_member_id", columnList = "member_id"),
           @Index(name = "idx_status", columnList = "status"),
           @Index(name = "idx_created_at", columnList = "created_at DESC")
       })
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class IngredientRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100, name = "ingredient_name")
    private String ingredientName; // 요청한 재료명
    
    @Column(nullable = false, length = 20, name = "request_type")
    private String requestType; // 'STORAGE' | 'PREPARATION'
    
    @Column(columnDefinition = "TEXT")
    private String message; // 사용자 메시지 (선택)
    
    @Column(name = "member_id")
    private Long memberId; // 요청한 사용자 ID (비로그인 시 null)
    
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING"; // PENDING, PROCESSED, REJECTED
    
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 비즈니스 로직
    public void updateStatus(String status) {
        this.status = status;
    }
}
