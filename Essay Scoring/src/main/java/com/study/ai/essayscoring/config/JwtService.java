package com.study.ai.essayscoring.config;

import com.study.ai.essayscoring.entity.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT 生成与解析工具
 */
@Slf4j
@Component
public class JwtService {

    /** HS256 要求密钥至少 256 位（32 字节） */
    private static final int MIN_KEY_BYTES = 32;

    @Value("${jwt.secret:}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    /**
     * ★ 启动即校验密钥，配置有问题直接拒绝启动。
     * 宁可启动失败，也不要带着弱密钥/可预测密钥上线。
     */
    @PostConstruct
    void validateSecret() {
        if (!StringUtils.hasText(jwtSecret)) {
            throw new IllegalStateException(
                    "JWT_SECRET 未配置。请在环境变量中提供 Base64 编码的密钥。"
                            + "生成方式：python -c \"import os,base64;print(base64.b64encode(os.urandom(48)).decode())\"");
        }
        byte[] bytes;
        try {
            bytes = Decoders.BASE64.decode(jwtSecret);
        } catch (Exception e) {
            throw new IllegalStateException("JWT_SECRET 不是合法的 Base64 字符串", e);
        }
        if (bytes.length < MIN_KEY_BYTES) {
            throw new IllegalStateException(String.format(
                    "JWT_SECRET 解码后仅 %d 字节，HS256 要求至少 %d 字节", bytes.length, MIN_KEY_BYTES));
        }
        log.info("JWT 密钥校验通过（{} 字节）", bytes.length);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserAccount user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("displayName", user.getDisplayName());
        claims.put("studentId", user.getStudentId());
        return generateToken(claims, user.getUsername());
    }

    public String generateToken(Map<String, Object> extraClaims, String subject) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
