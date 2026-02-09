package com.knusrae.cook.api.recipe.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookmark_folder",
       indexes = {
           @Index(name = "idx_member_id", columnList = "member_id"),
           @Index(name = "idx_member_sort", columnList = "member_id, sort_order")
       })
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class BookmarkFolder {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, name = "member_id")
    private Long memberId;
    
    @Column(nullable = false, length = 50)
    private String name;
    
    @Column(length = 20)
    @Builder.Default
    private String color = "blue"; // 기본 색상
    
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
    
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 명시적 업데이트 메서드
    public void updateFolder(String name, String color) {
        this.name = name;
        this.color = color;
    }
    
    public void updateSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
