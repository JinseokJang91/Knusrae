package com.knusrae.member.api.domain.repository;

import com.knusrae.member.api.domain.entity.Inquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    Page<Inquiry> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    Page<Inquiry> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Optional<Inquiry> findByIdAndMemberId(Long id, Long memberId);

    @Query("SELECT i FROM Inquiry i LEFT JOIN FETCH i.images WHERE i.id = :id AND i.memberId = :memberId")
    Optional<Inquiry> findByIdAndMemberIdWithImages(Long id, Long memberId);

    @Query("SELECT i FROM Inquiry i LEFT JOIN FETCH i.images WHERE i.id = :id")
    Optional<Inquiry> findByIdWithImages(Long id);

    @Query("SELECT i FROM Inquiry i LEFT JOIN FETCH i.reply WHERE i.id = :id AND i.memberId = :memberId")
    Optional<Inquiry> findByIdAndMemberIdWithReply(Long id, Long memberId);
}
