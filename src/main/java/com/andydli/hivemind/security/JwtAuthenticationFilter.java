package com.andydli.hivemind.security;

import org.springframework.stereotype.Component;
import com.andydli.hivemind.repository.UserRepository;
import com.andydli.hivemind.model.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        // no JWT token, continue filter chain and return
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // extract token from header
        String token = authHeader.substring(7);
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

            // build authenticated user token and set in security context
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    user, null, Collections.emptyList());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            logger.warn("JWT Authentication Processing Failed: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}