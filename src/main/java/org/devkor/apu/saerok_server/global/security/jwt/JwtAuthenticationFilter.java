package org.devkor.apu.saerok_server.global.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.global.security.principal.UserPrincipal;
import org.devkor.apu.saerok_server.global.security.token.AccessTokenProvider;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AccessTokenProvider accessTokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            try {
                Long userId = accessTokenProvider.getUserId(token);
                List<String> roles = accessTokenProvider.getUserRoles(token);

                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .toList();

                UserPrincipal userPrincipal = new UserPrincipal(userId);
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userPrincipal, null, authorities
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);

//                log.info("JWT 인증 성공 - id: {}, roles: {}", userId, roles);
            } catch (Exception e) {
                log.warn("JWT 인증 실패: {}", e.getMessage());

                jwtAuthenticationEntryPoint.commence(request, response, new InsufficientAuthenticationException("JWT Invalid", e));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
