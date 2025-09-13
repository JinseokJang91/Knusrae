package com.knusrae.auth.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GoogleUserDTO {
    private String id;
    private String email;
    private String name;
    @JsonProperty("given_name")
    private String givenName;
    @JsonProperty("family_name")
    private String familyName;
    private String picture;
    @JsonProperty("verified_email")
    private boolean verifiedEmail;
}