package com.knusrae.member.api.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inquiry_image", indexes = @Index(name = "idx_inquiry_image_inquiry_id", columnList = "inquiry_id"))
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InquiryImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id", nullable = false)
    private Inquiry inquiry;

    @Column(name = "image_url", nullable = false, length = 512)
    private String imageUrl;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    public static InquiryImage of(Inquiry inquiry, String imageUrl, int sortOrder) {
        InquiryImage image = new InquiryImage();
        image.inquiry = inquiry;
        image.imageUrl = imageUrl;
        image.sortOrder = sortOrder;
        return image;
    }
}
