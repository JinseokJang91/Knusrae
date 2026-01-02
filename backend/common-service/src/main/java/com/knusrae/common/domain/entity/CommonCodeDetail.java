package com.knusrae.common.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "common_code_detail")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class CommonCodeDetail {

    @EmbeddedId
    private CommonCodeDetailId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("codeId")
    @JoinColumn(name = "code_id", nullable = false)
    private CommonCode code;

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

    public String getCodeId() {
        return id != null ? id.getCodeId() : null;
    }

    public String getDetailCodeId() {
        return id != null ? id.getDetailCodeId() : null;
    }

    protected void setCode(CommonCode code) {
        this.code = code;
    }
}

