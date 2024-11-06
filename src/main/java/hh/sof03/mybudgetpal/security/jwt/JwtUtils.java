package hh.sof03.mybudgetpal.security.jwt;

import java.security.Key;
import java.util.Date;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import hh.sof03.mybudgetpal.security.services.CustomUserDetails;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {
  private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

  @Value("${app.jwtSecret}")
  private String jwtSecret;

  @Value("${app.jwtExpirationMs}")
  private int jwtExpirationMs;

  @Value("${app.jwtCookieName}")
  private String jwtCookie;

  private Key key;

  @PostConstruct
  public void init() {
    this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
  }
  
  /**
   * Get JWT token from cookies
   * 
   * @param request
   * @return String
   */
  public String getJwtFromCookies(HttpServletRequest request) {
    Cookie cookie = WebUtils.getCookie(request, jwtCookie);
    return (cookie != null) ? cookie.getValue() : null;
}

  /**
   * Get JWT token from header
   * 
   * @param request
   * @return String
   */
  public String getJwtFromHeader(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");
    return (authHeader != null && authHeader.startsWith("Bearer ")) ? authHeader.substring(7) : null;
  }

  /**
   * Generate JWT token
   * 
   * @param userDetails
   * @return String
   */
  public ResponseCookie generateJwtCookie(CustomUserDetails userDetails) {
    String jwt = generateTokenFromUser(userDetails);
    return ResponseCookie.from(jwtCookie, jwt).path("/api").maxAge(24 * 60 * 60).httpOnly(true).build();
  }

  /**
   * Get clean JWT cookie
   * 
   * @return ResponseCookie
   */
  public ResponseCookie getCleanJwtCookie() {
    ResponseCookie cookie = ResponseCookie.from(jwtCookie, null).path("/api").build();
    return cookie;
  }

  /**
   * Get email from JWT token
   * 
   * @param token
   * @return String
   */
  public String getEmailFromJwtToken(String token) {
    return Jwts.parserBuilder().setSigningKey(key).build()
        .parseClaimsJws(token).getBody().get("email", String.class);
  }

  /**
   * Get username from JWT token
   * 
   * @param token
   * @return String
   */
  public String getUserNameFromJwtToken(String token) {
    return Jwts.parserBuilder().setSigningKey(key).build()
        .parseClaimsJws(token).getBody().get("username", String.class);
  }
  
  /**
   * Validate JWT token
   * 
   * @param authToken
   * @return boolean
   */
  public boolean validateJwtToken(String authToken) {
    try {
      Jwts.parserBuilder().setSigningKey(key).build().parse(authToken);
      return true;
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

  /**
   * Generate JWT token from user
   * 
   * @param user
   * @return String
   */
  public String generateTokenFromUser(CustomUserDetails user) {
    return Jwts.builder()
              .claim("email", user.getEmail())
              .claim("username", user.getUsername())
              .setIssuedAt(new Date())
              .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
              .signWith(key, SignatureAlgorithm.HS256)
              .compact();
  }
  
}
