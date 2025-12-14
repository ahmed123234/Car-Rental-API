// ==== JWT Authentication Entry Point ====
package com.carrental.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);

    @Override
    public void commence(HttpServletRequest httpServletRequest,
                        HttpServletResponse httpServletResponse,
                        AuthenticationException e) throws IOException, ServletException {
        logger.error("Responding with unauthorized error. Message: {}", e.getMessage());

        httpServletResponse.setContentType("application/json;charset=UTF-8");
        httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("code", "UNAUTHORIZED");
        body.put("message", "Unauthorized: " + e.getMessage());
        body.put("timestamp", LocalDateTime.now());
        body.put("path", httpServletRequest.getServletPath());

        ObjectMapper mapper = new ObjectMapper();
        httpServletResponse.getWriter().write(mapper.writeValueAsString(body));
    }
}
