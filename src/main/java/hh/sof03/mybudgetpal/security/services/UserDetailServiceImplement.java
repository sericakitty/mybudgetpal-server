package hh.sof03.mybudgetpal.security.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import hh.sof03.mybudgetpal.MybudgetpalApplication;
import hh.sof03.mybudgetpal.domain.User;
import hh.sof03.mybudgetpal.domain.UserRepository;

@Service
@Configuration
public class UserDetailServiceImplement implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(MybudgetpalApplication.class);
  
    private final UserRepository userRepository;

    @Autowired
    public UserDetailServiceImplement(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Load user by username
     * 
     * @param username
     * @return UserDetails
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
      Optional<User> optionalUser = userRepository.findByUsername(username);
      
      if (!optionalUser.isPresent()) {
        throw new UsernameNotFoundException("No user found with username: " + username);
      }

      User user = optionalUser.get();
      
      logger.info("User found: " + user.getUsername());
      return UserDetailServiceImplement.build(user);
    }

    /**
     * Load user by email
     * @param email
     * @return UserDetails
     */
    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
      Optional<User> optionalUser = userRepository.findByEmail(email);
    
      if (!optionalUser.isPresent()) {
        throw new UsernameNotFoundException("No user found with email: " + email);
      }

      User user = optionalUser.get();
    
      return UserDetailServiceImplement.build(user);
    }

  /**
   * Build user details
   * 
   * @param user
   * @return CustomUserDetails
   */
  public static CustomUserDetails build(User user) {
    List<GrantedAuthority> authorities = user.getRoles().stream()
      .map(role -> new SimpleGrantedAuthority(role))
      .collect(Collectors.toList());

    return new CustomUserDetails(
      user.getId(),
      user.getUsername(),
      user.getEmail(),
      user.getPasswordHash(),
      authorities
    );
  }

}