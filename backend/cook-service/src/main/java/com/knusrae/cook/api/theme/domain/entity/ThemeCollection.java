package com.knusrae.cook.api.theme.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "theme_collection",
       indexes = {
           @Index(name = "idx_theme_status_sort", columnList = "status, sort_order"),
           @Index(name = "idx_theme_start_end_date", columnList = "start_date, end_date")
       })
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ThemeCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "thumbnail_image", length = 500)
    private String thumbnailImage;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<ThemeCollectionRecipe> recipes = new ArrayList<>();

    public void updateTheme(String name, String description, String thumbnailImage,
                             LocalDate startDate, LocalDate endDate, Integer sortOrder) {
        this.name = name;
        this.description = description;
        this.thumbnailImage = thumbnailImage;
        this.startDate = startDate;
        this.endDate = endDate;
        this.sortOrder = sortOrder;
    }

    public void updateStatus(String status) {
        this.status = status;
    }

    public boolean isActive() {
        if (!"ACTIVE".equals(status)) {
            return false;
        }
        LocalDate now = LocalDate.now();
        if (startDate != null && now.isBefore(startDate)) {
            return false;
        }
        if (endDate != null && now.isAfter(endDate)) {
            return false;
        }
        return true;
    }
}
