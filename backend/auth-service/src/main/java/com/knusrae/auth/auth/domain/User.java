package com.knusrae.auth.auth.domain;

import com.knusrae.auth.auth.dto.SocialRole;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {
    private String id;
    private String password;
    private String name;
    private String email;
    private String phone;
    private String address;
    private SocialRole role;
    private String status;
    private String createdAt;
    private String updatedAt;
}
