package hh.sof03.mybudgetpal.security.jwt;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import hh.sof03.mybudgetpal.security.services.UserDetailServiceImplement;

public class AuthTokenFilter extends OncePerRequestFilter {

  @Autowired
  private JwtUtils jwtUtils;

  @Autowired
  private UserDetailServiceImplement userDetailServiceImplement;

  private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

  /*
   * This method will be triggered for every incoming HTTP request and checks if there is a JWT sent in the request, and if it is valid.
   */
  @Override
protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {
  try {
    // Get JWT token from request
    String jwt = parseJwt(request);

    // If JWT token is not null and is valid, set the user authentication
    if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
      String userCred = jwtUtils.getEmailFromJwtToken(jwt);
      UserDetails userDetails;

      // Load user by email
      if (userCred != null) {
        userDetails = userDetailServiceImplement.loadUserByEmail(userCred);
      } else {
        // Load user by username
        userCred = jwtUtils.getUserNameFromJwtToken(jwt);
        userDetails = userDetailServiceImplement.loadUserByUsername(userCred);
      }

      // Create UsernamePasswordAuthenticationToken
      UsernamePasswordAuthenticationToken authentication = 
          new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

      // Set details in authentication
      authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

      // Set authentication in SecurityContextHolder
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }
  } catch (Exception e) {
    logger.error("Cannot set user authentication: {}", e);
  }

  filterChain.doFilter(request, response);
}
  
  /**
   * Parse JWT token from request
   * 
   * @param request
   * @return String
   */
  private String parseJwt(HttpServletRequest request) {
    String jwt = jwtUtils.getJwtFromHeader(request);
    return (jwt != null) ? jwt : jwtUtils.getJwtFromCookies(request);
}
}
