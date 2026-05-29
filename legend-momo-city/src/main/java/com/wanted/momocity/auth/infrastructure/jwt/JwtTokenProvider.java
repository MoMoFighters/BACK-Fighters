package com.wanted.momocity.auth.infrastructure.jwt;

import com.wanted.momocity.auth.application.port.TokenProviderPort;
import com.wanted.momocity.auth.domain.exception.InvalidTokenException;
import com.wanted.momocity.auth.infrastructure.exception.ExpiredJwtCustomException;
import com.wanted.momocity.auth.infrastructure.exception.InvalidJwtCustomException;
import com.wanted.momocity.auth.infrastructure.security.CustomUserDetails;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider implements TokenProviderPort {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration}")
    private long ACCESS_TOKEN_EXPIRE_TIME;

    @Value("${jwt.refresh-token-expiration}")
    private long REFRESH_TOKEN_EXPIRE_TIME;

    private SecretKey key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes); // 반환 타입: SecretKey
    }


    // 🎯 AccessToken 생성
//    ========================================================
    public String createAccessToken(Authentication authentication) { // 사용자 정보
        String username = authentication.getName();

        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", authorities)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRE_TIME))
                .signWith(SignatureAlgorithm.HS512, key) //  signWith만 전달하면 HS256 자동 적용
                .compact();

    }


    // 🎯 RefreshToken 생성
    public String createRefreshToken(String userId) {
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRE_TIME))
                .signWith(SignatureAlgorithm.HS512, key)
                .compact();
    }

    // ✅ 토큰 유효성 검사 (JJWT 0.12.x 방식)
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(key)
                    .parseClaimsJws(token);
            return true;

        } catch (ExpiredJwtException e) {
            throw new ExpiredJwtCustomException("Expired JWT token: " + e.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidJwtCustomException("Invalid JWT token: " + e.getMessage(), e);
        }
    }

    // 토큰에서 Claims 추출 (만료 예외 발생시키지 않음, 내부 사용)
    private Claims extractClaims(String token, boolean allowExpired) throws InvalidJwtCustomException {
        try {
            return Jwts.parser()
                    .setSigningKey(key)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            if (allowExpired) {
                return e.getClaims();
            }
            throw new InvalidJwtCustomException("Token expired and claims parsing not allowed for this context.", e);
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidJwtCustomException("Invalid JWT, cannot extract claims: " + e.getMessage(), e);
        }
    }


    // 토큰에서 사용자 정보 추출 (Authentication 객체 생성)
    public Authentication getAuthentication(String token) throws InvalidJwtCustomException {
        Claims claims = extractClaims(token, false); // 만료된 토큰은 여기서 걸러짐 (validateToken 이후 호출되므로)
        String userId = claims.getSubject();
        String rolesString = claims.get("roles", String.class);

        Collection<? extends GrantedAuthority> authorities = Collections.emptyList();
        if (rolesString != null && !rolesString.trim().isEmpty()) {
            authorities = Arrays.stream(rolesString.split(","))
                    .map(String::trim)
                    .filter(role -> !role.isEmpty())
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }

        CustomUserDetails customUserDetails = new CustomUserDetails(
                Long.parseLong(userId),  // ← CustomUserDetails로 교체
                "",
                authorities
        );

        return new UsernamePasswordAuthenticationToken(customUserDetails, token, authorities);
    }


    // 만료된 토큰 포함, 토큰에서 사용자 id 추출
    public String getIdFromToken(String token) throws InvalidJwtCustomException {
        try {
            return extractClaims(token, true).getSubject(); // allowExpired = true
        } catch (JwtException e) { // extractClaims가 InvalidJwtCustomException을 던지지만, 더 넓게 잡을 수 있음
            throw new InvalidJwtCustomException("Failed to get username from token: " + e.getMessage(), e);
        }
    }

    // ✅ 요청 헤더에서 JWT 토큰 추출
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // 요청 헤더에서 Refresh Token 추출 (예: "X-Refresh-Token" 헤더 사용)
    public String resolveRefreshToken(HttpServletRequest request) {
        return request.getHeader("Refresh-Token");
    }

    public long getRefreshTokenValidityMilliseconds() {
        return REFRESH_TOKEN_EXPIRE_TIME;
    }

    @Override
    public long getAccessTokenValidityMilliseconds() {
        return ACCESS_TOKEN_EXPIRE_TIME;
    }

    // 임시비밀번호 발급용 3분짜리 액세스 토큰 생성
    @Override
    public String createTempAccessToken(Authentication authentication) {
        String username = authentication.getName();
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", authorities)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3 * 60 * 1000L)) // 3분
                .signWith(SignatureAlgorithm.HS512, key)
                .compact();
    }

    // 소셜 로그인에서는 authentication이 없으니까 서비스에서 소셜로그인 하고 얻은
    @Override
    public String createAccessToken(String userId, String role) {
        return Jwts.builder()
                .setSubject(userId)
                .claim("roles", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRE_TIME))
                .signWith(SignatureAlgorithm.HS512, key)
                .compact();
    }


    // 액세스 토큰 블랙리스트 처리용 액세스 토큰 로그아웃 후 잔여시간 계산
    // 지금으로부터 만료까지 얼마나 남았는지를 밀리초로 계산
    @Override
    public long getRemainingMillis(String accessToken) {
        try {
            Date expiration = Jwts.parser()
                    .setSigningKey(key)
                    .parseClaimsJws(accessToken)
                    .getBody()
                    .getExpiration();
            return expiration.getTime() - System.currentTimeMillis();
            // expiration.getTime() : 만료되는 시각
        }catch (ExpiredJwtException e){
            return 0; // 이미 만료됐으면 0반환
        }catch (JwtException e){
            throw new InvalidTokenException("유효하지 않은 토큰입니다.");
        }
    }

}



/*
 * - 토큰 재사용 방지를 위해 Redis에 RefreshToken 저장 및 블랙리스트 처리 전략 필요
 * - key는 `@PostConstruct`에서 디코딩/변환 → `@Value`만 사용할 경우 Spring Context 순서에 따라 NullPointer 발생 가능
 * */
