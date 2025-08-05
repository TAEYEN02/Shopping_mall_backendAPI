package com.backend.shopping.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;


@Component
public class JwtTokenProvider {
	private final SecretKey key;
	private final long jwtExpiration;
	
	public JwtTokenProvider(@Value("${jwt.secret}") String secret,
							@Value("${jwt.expiration}") long jwtExpiration) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes());
		this.jwtExpiration=jwtExpiration;
	}
	
	//JWT 토큰 
	public String generateToken(Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Date expriyDate = new Date(System.currentTimeMillis()+jwtExpiration);
		
		return Jwts.builder()
				.setSubject(userPrincipal.getUsername())
				.setIssuedAt(new Date())
				.setExpiration(expriyDate)
				.signWith(key, SignatureAlgorithm.HS512)
				.compact();
	}
	
	public String getUsernameFromToken (String token) {
		Claims claims = Jwts.parserBuilder()
							.setSigningKey(key)
							.build()
							.parseClaimsJws(token)
							.getBody();
		return claims.getSubject();
	}
	
	//JWT 유효 검증
	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder()
				.setSigningKey(key)
				.build()
				.parseClaimsJws(token);
			return true;
		} catch (MalformedJwtException ex) {
			System.err.println("JWT 토큰 없음");
		}catch (ExpiredJwtException ex) {
			System.err.println("Expired JWT Token");
		}catch(UnsupportedJwtException ex) {
			System.err.println("Unsupported JWT token");
		}catch(IllegalArgumentException ex) {
			System.err.println("JWT claims string is empty");
		}
		return false;
	}
}
