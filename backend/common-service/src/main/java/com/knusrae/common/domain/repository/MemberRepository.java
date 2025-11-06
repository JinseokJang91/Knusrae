package com.knusrae.common.domain.repository;

import com.knusrae.common.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findById(Long id);
    Member findByEmail(String email);
}

