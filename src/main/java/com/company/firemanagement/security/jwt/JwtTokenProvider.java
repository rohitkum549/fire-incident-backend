package com.company.firemanagement.security.jwt;

import com.company.firemanagement.security.principal.UserPrincipal;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${app.security.jwt.secret}")
    private String jwtSecret;

    public boolean validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            // Verify signature using symmetric HMAC verifier
            JWSVerifier verifier = new MACVerifier(jwtSecret.getBytes());
            
            if (!signedJWT.verify(verifier)) {
                log.warn("JWT token signature validation failed.");
                return false;
            }

            // Verify expiration
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            Date expirationTime = claims.getExpirationTime();
            if (expirationTime != null && expirationTime.before(new Date())) {
                log.warn("JWT token has expired.");
                return false;
            }

            return true;
        } catch (ParseException e) {
            log.warn("Failed to parse JWT token: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Exception during JWT validation: {}", e.getMessage());
        }
        return false;
    }

    public Authentication getAuthentication(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            String userId = claims.getSubject();
            String email = claims.getStringClaim("email");
            if (email == null) {
                email = userId;
            }

            List<?> roles = claims.getStringListClaim("roles");
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            if (roles != null) {
                authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toString().toUpperCase()))
                        .collect(Collectors.toList());
            } else {
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            }

            UserPrincipal principal = new UserPrincipal(userId, email, authorities);
            return new UsernamePasswordAuthenticationToken(principal, token, authorities);
        } catch (ParseException e) {
            log.error("Could not build authentication from token: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid JWT token format", e);
        }
    }
}
