package com.knusrae.common.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class SecurityHandlers {
    private final ObjectMapper om = new ObjectMapper();

    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, ex) -> writeJson(response, 401, "UNAUTHORIZED");
    }
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, ex) -> writeJson(response, 403, "FORBIDDEN");
    }

    private void writeJson(HttpServletResponse res, int status, String msg) throws IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        om.writeValue(res.getWriter(), Map.of("error", msg));
    }
}
