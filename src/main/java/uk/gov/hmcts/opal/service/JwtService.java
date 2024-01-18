package uk.gov.hmcts.opal.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    private final String secretKey;
    private static final long EXPIRATION_TIME_MS = 3600000; // 1 hour in milliseconds

    public JwtService(
        @Value("${spring.security.oauth2.client.registration.internal-azure-ad.client-secret}") String secretKey) {
        this.secretKey = secretKey;
    }

    public String generateJwtToken(String username) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION_TIME_MS);

        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(expiration)
//            .signWith(SignatureAlgorithm.HS256, this.secretKey)
            .compact();
    }
}
