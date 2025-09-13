package com.knusrae.auth.api.service.request;

import jakarta.validation.constraints.NotBlank;

public record TokenRequest(@NotBlank String username, @NotBlank String password) {
}
