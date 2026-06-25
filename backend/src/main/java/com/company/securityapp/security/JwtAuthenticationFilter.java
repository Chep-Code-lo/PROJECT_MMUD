package com.company.securityapp.security;

import com.company.securityapp.entity.User;
import com.company.securityapp.service.AuditLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            CustomUserDetailsService userDetailsService,
            AuditLogService auditLogService,
            ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || authHeader.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!authHeader.startsWith("Bearer ")) {
            auditLogService.logSecurityEvent(
                    "TOKEN_REJECTED",
                    "Request",
                    null,
                    "FAILED",
                    "Authorization header must use Bearer token format.");
            writeUnauthorized(response, request.getRequestURI(), "Authorization header must use Bearer token format.");
            return;
        }

        String token = authHeader.substring(7).trim();
        if (token.isBlank()) {
            auditLogService.logSecurityEvent(
                    "TOKEN_REJECTED",
                    "Request",
                    null,
                    "FAILED",
                    "Bearer token value is missing.");
            writeUnauthorized(response, request.getRequestURI(), "Bearer token value is missing.");
            return;
        }

        try {
            JwtAccessTokenClaims claims = jwtService.parseAccessToken(token);
            User user = userDetailsService.loadDomainUserById(claims.userId());

            if (!user.getEmail().equalsIgnoreCase(claims.email())) {
                throw new TokenValidationException("JWT email claim does not match the current user record.");
            }

            if (user.getRole() != claims.role()) {
                throw new TokenValidationException("JWT role claim does not match the current user role.");
            }

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
            for (String scope : claims.scopes()) {
                authorities.add(new SimpleGrantedAuthority("SCOPE_" + scope));
            }

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(user, null, authorities);
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            filterChain.doFilter(request, response);
        } catch (TokenValidationException exception) {
            SecurityContextHolder.clearContext();
            auditLogService.logSecurityEvent("TOKEN_REJECTED", "Request", null, "FAILED", exception.getMessage());
            writeUnauthorized(response, request.getRequestURI(), exception.getMessage());
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String path, String message) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("error", HttpStatus.UNAUTHORIZED.getReasonPhrase());
        body.put("message", message);
        body.put("path", path);

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
