package com.knusrae.auth.auth.repository;

import io.micrometer.observation.ObservationFilter;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {
    public ObservationFilter findByUsername(String username) {
    }
}
