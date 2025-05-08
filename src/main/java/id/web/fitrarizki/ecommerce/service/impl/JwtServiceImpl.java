package id.web.fitrarizki.ecommerce.service.impl;

import id.web.fitrarizki.ecommerce.config.AppProp;
import id.web.fitrarizki.ecommerce.model.UserInfo;
import id.web.fitrarizki.ecommerce.service.JwtService;
import id.web.fitrarizki.ecommerce.util.DateUtil;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtServiceImpl implements JwtService {
    private final AppProp appProp;

    @Override
    public String generateToken(UserInfo userInfo) {
        Date expiration = DateUtil.convertLocalDateTimeToDate(LocalDateTime.now().plus(appProp.getJwt().getExpirationTime()));

        return Jwts.builder()
                .setSubject(userInfo.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(expiration)
                .signWith(signKey())
                .compact();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            JwtParser parser = Jwts.parserBuilder().setSigningKey(signKey()).build();
            parser.parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getUsernameFromToken(String token) {
        JwtParser parser = Jwts.parserBuilder().setSigningKey(signKey()).build();

        return parser.parseClaimsJws(token).getBody().getSubject() ;
    }

    private SecretKey signKey() {
        return Keys.hmacShaKeyFor(appProp.getJwt().getSecretKey().getBytes(StandardCharsets.UTF_8));
    }
}
