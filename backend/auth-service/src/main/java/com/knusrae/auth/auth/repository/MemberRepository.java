package com.knusrae.auth.auth.repository;

import com.knusrae.auth.auth.domain.Member;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Member findByEmail(String email);

    @NotNull
    @Override
    <T extends Member> T save(@NotNull T entity);
}
