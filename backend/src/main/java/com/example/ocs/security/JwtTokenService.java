package com.example.ocs.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {
  private final JwtProperties properties;
  private final Algorithm algorithm;
  private final JWTVerifier verifier;

  public JwtTokenService(JwtProperties properties) {
    this.properties = properties;
    this.algorithm = Algorithm.HMAC256(properties.secret());
    this.verifier = JWT.require(algorithm).withIssuer(properties.issuer()).build();
  }

  public String generateToken(long userId, String username, List<String> roleCodes) {
    Instant now = Instant.now();
    Instant expiresAt = now.plusSeconds(properties.expiresMinutes() * 60);
    return JWT.create()
        .withIssuer(properties.issuer())
        .withSubject(username)
        .withIssuedAt(Date.from(now))
        .withExpiresAt(Date.from(expiresAt))
        .withClaim("uid", userId)
        .withClaim("roles", roleCodes)
        .sign(algorithm);
  }

  public DecodedJWT verify(String token) throws JWTVerificationException {
    return verifier.verify(token);
  }
}

