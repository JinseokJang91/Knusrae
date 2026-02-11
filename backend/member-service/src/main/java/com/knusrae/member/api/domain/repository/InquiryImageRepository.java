package com.knusrae.member.api.domain.repository;

import com.knusrae.member.api.domain.entity.InquiryImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InquiryImageRepository extends JpaRepository<InquiryImage, Long> {
    List<InquiryImage> findByInquiryIdOrderBySortOrder(Long inquiryId);

    void deleteByInquiryId(Long inquiryId);
}
