package com.skillcraft.notification.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Mints short-lived JWTs signed with the same secret api-gateway/management
 * share, so this service can call management's API directly instead of
 * reading its database. There's no logged-in user behind a Kafka listener,
 * so the token carries a fixed service identity rather than a real user id -
 * subject "0" is reserved (real user ids start at 1) and never collides with
 * one.
 */
@Component
public class ServiceJwtIssuer {

	private static final long TOKEN_LIFETIME_MS = 60_000;

	private final SecretKey key;

	public ServiceJwtIssuer(@Value("${jwt.secret}") String secret) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	public String issueServiceToken() {
		Instant now = Instant.now();
		return Jwts.builder()
				.subject("0")
				.claim("role", "SERVICE")
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plusMillis(TOKEN_LIFETIME_MS)))
				.signWith(key)
				.compact();
	}
}
