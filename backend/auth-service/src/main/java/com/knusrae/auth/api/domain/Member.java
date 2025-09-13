package com.knusrae.auth.api.domain;

import com.knusrae.auth.api.dto.Gender;
import com.knusrae.auth.api.dto.SocialRole;
import com.knusrae.auth.api.dto.MemberState;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Member {
    @Id @GeneratedValue
    private Long id;

    @NotNull
    @Size(max = 50)
    private String email;
    @Size(max = 20)
    private String name;
    @Size(max = 11)
    private String phone;
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 10)
    private SocialRole role;
    @Enumerated(EnumType.STRING)
    @Column(name = "state", length = 10)
    private MemberState state;
    @Size(max = 8)
    private String birth;
    @Size(max = 50)
    private String createdAt;
    @Size(max = 50)
    private String updatedAt;
}
