package com.knusrae.auth.api.dto;

import lombok.Data;

@Data
public class NaverUserDTO {
    private String id;
    private String nickname;
    private String name;
    private String email;
    private String gender;
    private String age;
    private String birthyear;
    private String birthday;
    private String mobile;
    private String profileImage;
}
