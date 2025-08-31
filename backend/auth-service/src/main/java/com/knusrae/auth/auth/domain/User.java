package com.knusrae.auth.auth.domain;

import com.knusrae.auth.auth.dto.Gender;
import com.knusrae.auth.auth.dto.SocialRole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {
    private String id;
    private String email;
    private String name;
    private String phone;
    private Gender gender;
    private SocialRole role;
    private String state;
    private String birth;
    private String createdAt;
    private String updatedAt;
}
