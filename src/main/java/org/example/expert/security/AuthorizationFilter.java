package org.example.expert.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.config.JwtUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Slf4j(topic = "JWT Authorization")
public class AuthorizationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    public AuthorizationFilter(JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request);

        if (token != null) {
            if (jwtUtil.validateToken(token)) {
                Claims claims = jwtUtil.getUserInfoFromToken(token);
                if (claims != null) {
                    setAuthentication(claims);
                } else {
                    log.error("Token parsing failed");
                }
            } else {
                log.error("Invalid JWT token");
            }
        }

        filterChain.doFilter(request, response);
    }

    // Request로부터 토큰을 추출하는 메서드
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(jwtUtil.getBearerPrefix())) {
            return bearerToken.substring(jwtUtil.getBearerPrefix().length());
        }
        return null;
    }

    // Authentication 객체를 생성하여 SecurityContext에 저장하는 메서드
    private void setAuthentication(Claims claims) {
        String email = claims.get("email", String.class);
        if (email != null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("Security Context에 '{}' 인증 정보를 저장했습니다.", email);
        } else {
            log.error("Email not found in JWT token");
        }
    }
}