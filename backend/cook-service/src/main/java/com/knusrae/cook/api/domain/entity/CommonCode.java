package com.knusrae.cook.api.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "common_code")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class CommonCode {

    @Id
    @Column(name = "code_id", length = 30)
    private String codeId;

    @Column(name = "code_group", length = 30, nullable = false)
    private String codeGroup;

    @Column(name = "code_name", length = 50, nullable = false)
    private String codeName;

    @Column(name = "sort")
    private Integer sort;

    @Column(name = "use_yn", length = 2)
    private String useYn;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "code", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CommonCodeDetail> details = new ArrayList<>();

    public void addDetail(CommonCodeDetail detail) {
        this.details.add(detail);
        detail.setCode(this);
    }
}

