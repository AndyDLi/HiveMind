package com.andydli.hivemind.security;

import org.springframework.stereotype.Component;
import com.andydli.hivemind.repository.UserRepository;
import com.andydli.hivemind.model.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;

import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    // runs on every HTTP request that requires authentication
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token = extractTokenFromCookie(request);
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // validate token
            if (!jwtService.validateToken(token)) {
                logger.debug("Invalid or Expired JWT Token");
                filterChain.doFilter(request, response);
                return;
            }

            // extract user ID from token and load user
            Long userId = jwtService.extractUserId(token);
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                logger.debug("JWT References Non-Existent User ID: " + userId);
                filterChain.doFilter(request, response);
                return;
            }

            // build authenticated user token: user = principal, null = credentials, emptyList = authorities
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    user, null, Collections.emptyList());

            // attaches HTTP metadata to the token
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // set the authentication in the security context
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            logger.warn("JWT Authentication Processing Failed: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        return Arrays.stream(cookies)
                .filter(cookie -> "token".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}