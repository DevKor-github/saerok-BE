package org.devkor.apu.saerok_server.global.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.devkor.apu.saerok_server.global.exception.JwtAuthException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secret;

    public String createAccessToken(Long userId, List<String> roles) {

        long expirationMillisecond = 1000L * 60 * 60 * 24 * 7; // 7일
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillisecond);

        return JWT.create()
                .withSubject(userId.toString())
                .withClaim("roles", roles)
                .withIssuedAt(now)
                .withExpiresAt(expiry)
                .sign(Algorithm.HMAC256(secret));
    }

    private DecodedJWT verifyAndDecode(String token) {
        try {
            return JWT.require(Algorithm.HMAC256(secret))
                    .build()
                    .verify(token);
        } catch (SignatureVerificationException e) {
            throw new JwtAuthException("토큰 서명 검증에 실패했습니다.");
        } catch (TokenExpiredException e) {
            throw new JwtAuthException("만료된 토큰입니다.");
        } catch (JWTVerificationException e) {
            throw new JwtAuthException("유효하지 않은 토큰입니다.");
        }

    }

    public Long getUserId(String token) {
        DecodedJWT jwt = verifyAndDecode(token);
        return Long.parseLong(jwt.getSubject());
    }

    public List<String> getUserRoles(String token) {
        DecodedJWT jwt = verifyAndDecode(token);
        return jwt.getClaim("roles").asList(String.class);
    }
}
