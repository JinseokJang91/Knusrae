package com.knusrae.auth.api.domain.repository;

import com.knusrae.auth.api.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Member findByEmail(String email);
}
