package com.knusrae.common.domain.repository;

import com.knusrae.common.domain.entity.Member;
import com.knusrae.common.domain.enums.SocialRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findById(Long id);
    Optional<Member> findByEmail(String email);
    Optional<Member> findByEmailAndSocialRole(String email, SocialRole socialRole);
}

