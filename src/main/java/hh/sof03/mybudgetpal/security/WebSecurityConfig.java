
package hh.sof03.mybudgetpal.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import hh.sof03.mybudgetpal.security.services.UserDetailServiceImplement;
import hh.sof03.mybudgetpal.security.jwt.AuthTokenFilter;
import hh.sof03.mybudgetpal.security.jwt.HttpRequestLoggingFilter;
import hh.sof03.mybudgetpal.security.jwt.AuthEntryPointJwt;
import hh.sof03.mybudgetpal.config.DotenvConfig;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig{
  @Autowired
  UserDetailServiceImplement userDetailServiceImplement;

  @Autowired
  private AuthEntryPointJwt unauthorizedHandler;

  @Autowired
  private HttpRequestLoggingFilter httpRequestLoggingFilter;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private DotenvConfig dotEnvConfig;

  @Bean
  public AuthTokenFilter authenticationJwtTokenFilter() {
    return new AuthTokenFilter();
  }

  @Bean
  public AuthenticationManager authenticationManagerBean(AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }


  @Autowired
  public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
      auth.userDetailsService(userDetailServiceImplement).passwordEncoder(passwordEncoder);
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
      http
              .csrf(csrf -> csrf.disable())
              .exceptionHandling(handling -> handling.authenticationEntryPoint(unauthorizedHandler))
              .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
              .authorizeHttpRequests(authorize -> authorize
              .requestMatchers("/request-password-reset", "/signup", "/login", "/verify-email-token", "/reset-password", "/auth/logout").permitAll()
              .requestMatchers("/auth/**", "/user/**", "/api/**").authenticated()
              .anyRequest().authenticated())
              .cors(cors -> cors.configurationSource(corsConfigurationSource()));

      http.addFilterBefore(httpRequestLoggingFilter, UsernamePasswordAuthenticationFilter.class);
      http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

      return http.build();
  }

  /**
   * CORS configuration
   * 
   * @return CorsConfigurationSource
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
      // Create CORS configuration
      CorsConfiguration config = new CorsConfiguration();
      config.setAllowCredentials(true);

      // Retrieve origins from environment variables
      String webCorsOrigins = dotEnvConfig.dotenv().get("WEB_CORS_ORIGIN");
      String mobileDeviceCorsOrigins = dotEnvConfig.dotenv().get("MOBILE_CORS_ORIGIN");
      String mobileEmulatorCorsOrigins = dotEnvConfig.dotenv().get("MOBILE_EMULATOR_CORS_ORIGIN");

      // Collect all origins into a list
      List<String> allowedOrigins = new ArrayList<>();

      if (webCorsOrigins != null && !webCorsOrigins.isEmpty()) {
          allowedOrigins.addAll(Arrays.asList(webCorsOrigins.split(",")));
      }

      if (mobileDeviceCorsOrigins != null && !mobileDeviceCorsOrigins.isEmpty()) {
          allowedOrigins.addAll(Arrays.asList(mobileDeviceCorsOrigins.split(",")));
      }

      if (mobileEmulatorCorsOrigins != null && !mobileEmulatorCorsOrigins.isEmpty()) {
          allowedOrigins.addAll(Arrays.asList(mobileEmulatorCorsOrigins.split(",")));
      }

      // Add "null" to allowed origins to handle requests with null Origin header
      allowedOrigins.add("null");

      // Trim and set allowed origins
      config.setAllowedOrigins(allowedOrigins.stream().map(String::trim).toList());

      // Allow all headers and methods
      config.addAllowedHeader(CorsConfiguration.ALL);
      config.addAllowedMethod(CorsConfiguration.ALL);

      // Expose any headers if necessary
      config.addExposedHeader("Authorization");
      config.addExposedHeader("X-Client-Type");

      // Register the configuration for all paths
      UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
      source.registerCorsConfiguration("/**", config);

      return source;
  }

}