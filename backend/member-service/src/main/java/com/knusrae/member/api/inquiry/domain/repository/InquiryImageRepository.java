package com.knusrae.member.api.inquiry.domain.repository;

import com.knusrae.member.api.inquiry.domain.entity.InquiryImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InquiryImageRepository extends JpaRepository<InquiryImage, Long> {
    @Query("SELECT i FROM InquiryImage i WHERE i.inquiry.id = :inquiryId ORDER BY i.sortOrder")
    List<InquiryImage> findByInquiryIdOrderBySortOrder(@Param("inquiryId") Long inquiryId);

    @Modifying
    @Query("DELETE FROM InquiryImage i WHERE i.inquiry.id = :inquiryId")
    void deleteByInquiryId(@Param("inquiryId") Long inquiryId);
}
