package com.knusrae.auth.auth.repository;

import com.knusrae.auth.auth.domain.User;
import com.knusrae.auth.auth.dto.SocialRole;
import io.micrometer.observation.ObservationFilter;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepository {

    public <T> Optional<T> findByEmail(String email) {

        return Optional.empty();
    }

    public User save(User user) {
        user.setId("testid");
        user.setName("장진석");
        user.setEmail("hya2089@gmail.com");
        user.setPhone("010-9901-9886");
        user.setRole(SocialRole.NAVER);
        return user;
    }
}
