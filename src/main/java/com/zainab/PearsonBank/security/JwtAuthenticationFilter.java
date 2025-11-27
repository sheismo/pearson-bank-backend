package com.zainab.PearsonBank.security;

import com.zainab.PearsonBank.entity.UserSession;
import com.zainab.PearsonBank.repository.SessionRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private SessionRepository sessionRepository;

    private static final int INACTIVITY_TIMEOUT_MINUTES = 10;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authorizationHeader.substring(7);

        String username;
        try {
            username = jwtTokenProvider.extractUsername(jwt);
            log.info("User email is: {}", username);
        } catch (Exception ex) {
            filterChain.doFilter(request, response);
            return;
        }

        UserSession session = sessionRepository.findByAccessToken(jwt);
        if(session == null || session.isRevoked()) {
            SecurityContextHolder.clearContext();
            sendUnauthorized(response, "Session Invalid or revoked!");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        boolean inactive = session.getLastActivity().plusMinutes(INACTIVITY_TIMEOUT_MINUTES).isBefore(now);
        boolean expired = session.getExpiresAt().isBefore(now);

        // Check inactivity
        if(inactive || expired ) {
            session.setRevoked(true);
            sessionRepository.save(session);

            SecurityContextHolder.clearContext();
            sendUnauthorized(response, "Session timed out!");
            return;
        }

        if (!jwtTokenProvider.validateToken(jwt)) {
            session.setRevoked(true);
            sessionRepository.save(session);

            SecurityContextHolder.clearContext();
            sendUnauthorized(response, "Token Invalid or expired!");
            return;
        }

        // Update last activity
        session.setLastActivity(LocalDateTime.now());
        sessionRepository.save(session);

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\": \"" + message + "\"}");
    }
}
