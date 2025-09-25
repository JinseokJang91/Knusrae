package com.knusrae.cook.api.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "category")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long depth;

    @NotNull
    @Column(nullable = false, length = 100)
    private String name;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 자기 참조 관계 (부모-자식)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("name ASC")
    @Builder.Default
    private List<Category> children = new ArrayList<>();

    // 비즈니스 메서드
    public void updateName(String name) {
        this.name = name;
    }

    public void updateParent(Category parent) {
        this.parent = parent;
        this.depth = parent != null ? parent.getDepth() + 1 : 0L;
    }

    // 연관관계 편의 메서드
    public void addChild(Category child) {
        this.children.add(child);
        child.setParent(this);
    }

    public void setParent(Category parent) {
        this.parent = parent;
        this.depth = parent != null ? parent.getDepth() + 1 : 0L;
    }

    // 루트 카테고리인지 확인
    public boolean isRoot() {
        return parent == null;
    }

    // 리프 카테고리인지 확인
    public boolean isLeaf() {
        return children.isEmpty();
    }
}
