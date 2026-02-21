package com.knusrae.member.api.inquiry.domain.repository;

import com.knusrae.member.api.inquiry.domain.entity.InquiryReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InquiryReplyRepository extends JpaRepository<InquiryReply, Long> {
    @Query("SELECT r FROM InquiryReply r WHERE r.inquiry.id = :inquiryId")
    Optional<InquiryReply> findByInquiryId(@Param("inquiryId") Long inquiryId);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM InquiryReply r WHERE r.inquiry.id = :inquiryId")
    boolean existsByInquiryId(@Param("inquiryId") Long inquiryId);

    @Query("SELECT r.inquiry.id FROM InquiryReply r WHERE r.inquiry.id IN :inquiryIds")
    List<Long> findInquiryIdsByInquiryIdIn(@Param("inquiryIds") List<Long> inquiryIds);
}
