package com.knusrae.member.api.domain.repository;

import com.knusrae.member.api.domain.entity.InquiryReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InquiryReplyRepository extends JpaRepository<InquiryReply, Long> {
    Optional<InquiryReply> findByInquiryId(Long inquiryId);

    boolean existsByInquiryId(Long inquiryId);

    @Query("SELECT r.inquiry.id FROM InquiryReply r WHERE r.inquiry.id IN :inquiryIds")
    List<Long> findInquiryIdsByInquiryIdIn(@Param("inquiryIds") List<Long> inquiryIds);
}
