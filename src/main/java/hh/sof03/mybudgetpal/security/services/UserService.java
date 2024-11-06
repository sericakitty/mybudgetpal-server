package hh.sof03.mybudgetpal.security.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import hh.sof03.mybudgetpal.domain.User;
import hh.sof03.mybudgetpal.domain.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import hh.sof03.mybudgetpal.security.jwt.JwtUtils;


@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * Get authenticated user
     * 
     * @return User
     */
    public User getAuthenticatedUser() {
        // Get authentication from context holder
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // If authentication is not null and principal is instance of CustomUserDetails
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            // Find user by email
            Optional<User> userOptional = userRepository.findByEmail(userDetails.getEmail());

            // If user is present, return user
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                return user;
            }
            return null;
        }
        return null;
    }

    /**
     * Get user from request
     * 
     * @param request
     * @return User
     */
    public User getUserFromRequest(HttpServletRequest request) {
        
        String token = jwtUtils.getJwtFromHeader(request);  // Get token from header
        if (token == null || !jwtUtils.validateJwtToken(token)) {
            return null;
        }

        // Get username from token
        String username = jwtUtils.getUserNameFromJwtToken(token);

        if (username == null) {
            return null;
        }

        // Find user by username
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (!userOptional.isPresent()) {
            return null;
        }

        // Return user
        User user = userOptional.get();

        return user;
        
      }
}
