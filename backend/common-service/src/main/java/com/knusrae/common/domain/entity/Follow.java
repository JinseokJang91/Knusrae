package com.knusrae.common.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "follow",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_follower_following", columnNames = {"follower_id", "following_id"})
       },
       indexes = {
           @Index(name = "idx_follower", columnList = "follower_id"),
           @Index(name = "idx_following", columnList = "following_id"),
           @Index(name = "idx_created_at", columnList = "created_at DESC")
       })
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Follow {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, name = "follower_id")
    private Long followerId; // 팔로우하는 사람
    
    @Column(nullable = false, name = "following_id")
    private Long followingId; // 팔로우당하는 사람 (크리에이터)
    
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
