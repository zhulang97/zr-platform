package com.zhilian.zr.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenService {
  private final ZrSecurityProperties props;

  public JwtTokenService(ZrSecurityProperties props) {
    this.props = props;
  }

  public String mintAccessToken(long userId, String username, List<String> authorities) {
    Instant now = Instant.now();
    Instant exp = now.plusSeconds(props.accessTtlSeconds());
    return Jwts.builder()
        .setIssuer(props.issuer())
        .setSubject(Long.toString(userId))
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(exp))
        .addClaims(Map.of(
            "u", username,
            "a", authorities
        ))
        .signWith(signingKey(), Jwts.SIG.HS256)
        .compact();
  }

  public Claims parse(String jwt) {
    return Jwts.parser()
        .requireIssuer(props.issuer())
        .verifyWith(signingKey())
        .build()
        .parseSignedClaims(jwt)
        .getPayload();
  }

  private SecretKey signingKey() {
    // Keep it simple: if secret is base64, decode; otherwise use raw bytes.
    byte[] keyBytes;
    try {
      keyBytes = Decoders.BASE64.decode(props.secret());
    } catch (Exception ignore) {
      keyBytes = props.secret().getBytes(StandardCharsets.UTF_8);
    }
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
