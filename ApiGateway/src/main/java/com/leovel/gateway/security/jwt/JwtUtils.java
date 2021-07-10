package com.leovel.gateway.security.jwt;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.leovel.gateway.common.models.UserDTO;

import io.jsonwebtoken.*;

@Component
public class JwtUtils {
  private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

  @Value("${api.gateway.jwtSecret}")
  private String jwtSecret;

  @Value("${api.gateway.jwtExpirationMs}")
  private int jwtExpirationMs;
  
  //retrieve username from jwt token
  public String getUserNameFromJwtToken(String token) {
      return getClaimFromJwtToken(token, Claims::getSubject);
  }

  //retrieve expiration date from jwt token
  public Date getExpirationDateFromJwtToken(String token) {
      return getClaimFromJwtToken(token, Claims::getExpiration);
  }

  public <T> T getClaimFromJwtToken(String token, Function<Claims, T> claimsResolver) {
      final Claims claims = getAllClaimsFromToken(token);
      return claimsResolver.apply(claims);
  }


  //for retrieving any information from token we will need the secret key
  public Claims getAllClaimsFromToken(String token) {
      return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
  }

  public String generateJwtToken(UserDTO user) {
    
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", user.getRole());

    return Jwts.builder().setClaims(claims).setSubject((user.getUsername())).setIssuedAt(new Date())
        .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs)).signWith(SignatureAlgorithm.HS512, jwtSecret)
        .compact();
  }

  public boolean validateJwtToken(String authToken) {
    try {
      Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
      return true;
    } catch (SignatureException e) {
      logger.error("Invalid JWT signature: {}", e.getMessage());
    } catch (MalformedJwtException e) {
      logger.error("Invalid JWT token: {}", e.getMessage());
    } catch (ExpiredJwtException e) {
      logger.error("JWT token is expired: {}", e.getMessage());
    } catch (UnsupportedJwtException e) {
      logger.error("JWT token is unsupported: {}", e.getMessage());
    } catch (IllegalArgumentException e) {
      logger.error("JWT claims string is empty: {}", e.getMessage());
    }

    return false;
  }
}
