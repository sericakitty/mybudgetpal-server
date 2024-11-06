
package hh.sof03.mybudgetpal.controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.util.Set;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.mail.internet.MimeMessage;

import io.github.cdimascio.dotenv.Dotenv;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hh.sof03.mybudgetpal.payload.request.SignupRequest;
import hh.sof03.mybudgetpal.payload.request.LoginRequest;
import hh.sof03.mybudgetpal.payload.request.UserEmailTokenRequest;
import hh.sof03.mybudgetpal.payload.request.ResetPasswordRequest;
import hh.sof03.mybudgetpal.payload.response.UserInfoResponse;
import hh.sof03.mybudgetpal.payload.response.MessageResponse;
import hh.sof03.mybudgetpal.security.jwt.JwtUtils;
import hh.sof03.mybudgetpal.domain.UserRepository;
import hh.sof03.mybudgetpal.security.services.CustomUserDetails;
import hh.sof03.mybudgetpal.security.services.UserDetailServiceImplement;
import hh.sof03.mybudgetpal.MybudgetpalApplication;
import hh.sof03.mybudgetpal.domain.User;

import hh.sof03.mybudgetpal.config.ClientInfoConfig;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
public class AuthController {

  private static final Logger logger = LoggerFactory.getLogger(MybudgetpalApplication.class);

  @Autowired
  private ClientInfoConfig clientInfoConfig;

  @Autowired
  UserRepository userRepository;

  @Autowired
  AuthenticationManager authenticationManager;

  @Autowired
  BCryptPasswordEncoder bCryptPasswordEncoder;

  @Autowired
  JavaMailSender mailSender;

  @Autowired
  UserDetailServiceImplement userDetailServiceImplement;

  @Autowired
  Dotenv dotenv;

  @Autowired
  JwtUtils jwtUtils;

  /**
   * Authenticates the user and logs them in. Returns the user information and a
   * JWT token.
   * 
   * @param loginRequest the login request
   * @return the user information and a JWT token
   * @throws MessagingException if an error occurs while sending the email
   */
  @PostMapping("/login")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request)
      throws MessagingException {
    CustomUserDetails userDetails;

    if (loginRequest.getIdentifier().contains("@")) {
      userDetails = (CustomUserDetails) userDetailServiceImplement.loadUserByEmail(loginRequest.getIdentifier());
    } else {
      userDetails = (CustomUserDetails) userDetailServiceImplement.loadUserByUsername(loginRequest.getIdentifier());
    }

    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(userDetails.getUsername(), loginRequest.getPassword()));

    SecurityContextHolder.getContext().setAuthentication(authentication);

    ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

    List<String> roles = userDetails.getAuthorities().stream().map(item -> item.getAuthority())
        .collect(Collectors.toList());

    UserInfoResponse userInfoResponse = new UserInfoResponse(userDetails.getUsername(), userDetails.getEmail());
    userInfoResponse.setRoles(roles);
    userInfoResponse.setEnabled(userDetails.isEnabled());
    userInfoResponse.setToken(jwtCookie.getValue());

    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString()).body(userInfoResponse);
  }

  /**
   * Registers a new user.
   * 
   * @param signupRequest the signup request
   * @return a message response
   * @throws MessagingException if an error occurs while sending the email
   */
  @PostMapping("/signup")
  public ResponseEntity<MessageResponse> save(@Valid @RequestBody SignupRequest signupRequest,
      HttpServletRequest request)
      throws MessagingException {

    if (userRepository.findByEmail(signupRequest.getEmail()) != null) {
      return ResponseEntity.badRequest().body(new MessageResponse("Email already exists",  "error"));
    }

    if (!signupRequest.getFirstName().matches("^[a-zA-Z]*$")) {
      return ResponseEntity.badRequest().body(new MessageResponse("First name can only contain letters",  "error"));
    }

    if (!signupRequest.getLastName().matches("^[a-zA-Z]*$")) {
      return ResponseEntity.badRequest().body(new MessageResponse("Last name can only contain letters",  "error"));
    }

    if (userRepository.findByUsername(signupRequest.getUsername()) != null) {
      return ResponseEntity.badRequest().body(new MessageResponse("Username already exists",  "error"));
    }

    if (!signupRequest.getPassword().equals(signupRequest.getPasswordCheck())) {
      return ResponseEntity.badRequest().body(new MessageResponse("Passwords do not match",  "error"));
    }

    String newUsername = signupRequest.getUsername();
    String newFirstName = signupRequest.getFirstName();
    String newLastName = signupRequest.getLastName();
    String newEmail = signupRequest.getEmail();
    String pwd = signupRequest.getPassword();

    String hashPwd = bCryptPasswordEncoder.encode(pwd);

    UUID uuid = UUID.randomUUID();
    String verificationToken = uuid.toString().replaceAll("-", "");

    User newUser = new User();
    newUser.setPasswordHash(hashPwd);
    newUser.setUsername(newUsername);
    newUser.setFirstName(newFirstName);
    newUser.setLastName(newLastName);
    newUser.setEmail(newEmail);
    newUser.setEmailVerificationToken(verificationToken);
    newUser.setEmailVerificationTokenExpiryDate(LocalDateTime.now().plusHours(2));

    Set<String> roles = new HashSet<>();
    roles.add("ROLE_USER");
    newUser.setRoles(roles);

    userRepository.save(newUser);

    // Get the Origin header and determine the base URL
    String baseUrl = clientInfoConfig.determineBaseUrl(request);

    String verificationLink = baseUrl + "/verify-email?email=" + newEmail + "&token="
        + verificationToken;
    sendVerificationEmail(newUser.getEmail(), newUsername, verificationLink);

    return ResponseEntity.ok(new MessageResponse("User registered successfully! We have sent you a verification email. Please check your email.",  "success"));
  }

  /**
   * Logs out the user.
   * 
   * @return a message response
   */
  @GetMapping("/auth/logout")
  public ResponseEntity<MessageResponse> logoutUser() throws MessagingException {
    try {
      ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
      return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
          .body(new MessageResponse("User logged out successfully",  "success"));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(new MessageResponse("Error while logging out",  "error"));
    }
  }

  /**
   * Verifies the token of the user.
   * 
   * @param request the HTTP request
   * @return the user information
   * @throws MessagingException if an error occurs while sending the email
   */
  @GetMapping("/auth/validate-token")
  public ResponseEntity<UserInfoResponse> verifyToken(HttpServletRequest request) throws MessagingException {
    String token = jwtUtils.getJwtFromHeader(request);

    if (token != null && jwtUtils.validateJwtToken(token)) {

      String email = jwtUtils.getEmailFromJwtToken(token);

      Optional<User> userOptional = userRepository.findByEmail(email);

      if (!userOptional.isPresent()) {
        return ResponseEntity.badRequest().body(null);
      }

      User user = userOptional.get();

      if (user == null) {
        return ResponseEntity.badRequest().body(null);
      }

      List<String> roles = user.getRoles().stream().map(item -> item).collect(Collectors.toList());

      UserInfoResponse userInfoResponse = new UserInfoResponse(user.getUsername(), user.getEmail());
      userInfoResponse.setRoles(roles);
      userInfoResponse.setToken(token);
      userInfoResponse.setEnabled(user.isEnabled());

      return ResponseEntity.ok().body(userInfoResponse);
    }

    return ResponseEntity.badRequest().body(null);
  }

  /**
   * Retrieves user information from the token.
   * 
   * @param request the HTTP request
   * @return the user information
   * @throws MessagingException if an error occurs while sending the email
   */
  @GetMapping("/auth/user")
  public ResponseEntity<UserInfoResponse> getUserInfo(HttpServletRequest request) throws MessagingException {
    String token = jwtUtils.getJwtFromHeader(request);

    if (token != null && jwtUtils.validateJwtToken(token)) {

      String username = jwtUtils.getUserNameFromJwtToken(token);

      Optional<User> optionalUser = userRepository.findByUsername(username);

      if (!optionalUser.isPresent()) {
        return ResponseEntity.badRequest().body(null);
      }

      User user = optionalUser.get();


      List<String> roles = user.getRoles().stream().map(item -> item).collect(Collectors.toList());

      UserInfoResponse userInfoResponse = new UserInfoResponse(user.getUsername(),
          user.getEmail());

      userInfoResponse.setRoles(roles);
      userInfoResponse.setFirstName(user.getFirstName());
      userInfoResponse.setLastName(user.getLastName());
      userInfoResponse.setEnabled(user.isEnabled());

      return ResponseEntity.ok().body(userInfoResponse);
    }

    return ResponseEntity.badRequest().body(null);
  }

  /**
   * Sends a verification email to the user.
   * 
   * @param request the HTTP request
   * @throws MessagingException if an error occurs while sending the email
   */
  @GetMapping("/auth/resend-verification-email")
  public ResponseEntity<MessageResponse> requestEmailVerification(HttpServletRequest request)
      throws MessagingException {

    try {
      String token = jwtUtils.getJwtFromHeader(request);

      if (token != null && jwtUtils.validateJwtToken(token)) {
        String userEmail = jwtUtils.getEmailFromJwtToken(token);

        Optional<User> optionalUser = userRepository.findByEmail(userEmail);

        if (!optionalUser.isPresent()) {
          return ResponseEntity.badRequest().body(new MessageResponse("User not found",  "error"));
        }

        User user = optionalUser.get();

        if (user.isEnabled()) {
          return ResponseEntity.badRequest().body(new MessageResponse("User is already verified",  "error"));
        }

        UUID uuid = UUID.randomUUID();
        String verificationToken = uuid.toString().replaceAll("-", "");

        user.setEmailVerificationToken(verificationToken);
        user.setEmailVerificationTokenExpiryDate(LocalDateTime.now().plusHours(2));
        userRepository.save(user);

        // Determine the base URL based on the Origin header
        String baseUrl = clientInfoConfig.determineBaseUrl(request);

        String verificationLink = baseUrl + "/verify-email?email=" + userEmail + "&token="
            + verificationToken;
        sendVerificationEmail(userEmail, user.getUsername(), verificationLink);

        return ResponseEntity.ok(new MessageResponse("Verification email sent successfully",  "success"));
      }

      return ResponseEntity.badRequest().body(new MessageResponse("Invalid token",  "error"));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(new MessageResponse("Error while sending email",  "error"));
    }

  }

  /**
   * Verifies user's account by email and token
   * 
   * @param userEmailTokenRequest
   * @throws MessagingException if an error occurs while sending the email
   */
  @PostMapping("/verify-email-token")
  public ResponseEntity<MessageResponse> verifyEmail(@Valid @RequestBody UserEmailTokenRequest userEmailTokenRequest)
      throws MessagingException {
    String email = userEmailTokenRequest.getEmail();
    String emailVerificationToken = userEmailTokenRequest.getEmailVerificationToken();
    String passwordResetToken = userEmailTokenRequest.getPasswordResetToken();

    Optional<User> optionalUser = Optional.empty();
    if (!emailVerificationToken.isEmpty()) {
      optionalUser = userRepository.findByEmailAndEmailVerificationToken(email, emailVerificationToken);
    } else if (!passwordResetToken.isEmpty()) {
      optionalUser = userRepository.findByEmailAndPasswordResetToken(email, passwordResetToken);
    }

    if (!optionalUser.isPresent()) {
      return ResponseEntity.badRequest().body(new MessageResponse("Invalid token",  "error"));
    }

    User user = optionalUser.get();

    LocalDateTime tokenExpiryDate = (emailVerificationToken != null && !emailVerificationToken.isEmpty())
        ? user.getEmailVerificationTokenExpiryDate()
        : user.getPasswordResetTokenExpiryDate();

    if (tokenExpiryDate == null || tokenExpiryDate.isBefore(LocalDateTime.now())) {
      return ResponseEntity.badRequest().body(new MessageResponse("Token has expired. Please request a new verification email or password reset link.", "error"));
    }

    if (emailVerificationToken != null && !emailVerificationToken.isEmpty()) {
      user.setEnabled(true);
      user.setEmailVerificationToken(null);
      user.setEmailVerificationTokenExpiryDate(null);
    }

    userRepository.save(user);

    return ResponseEntity.ok()
        .body(new MessageResponse(emailVerificationToken != null && !emailVerificationToken.isEmpty() ? "Email verified successfully" : "Password reset token verified successfully", "success"));
  }

  /**
   * Sends an email with a verification link to the user.
   * 
   * @param toEmail         the email address of the user
   * @param username        the username of the user
   * @param confirmationUrl the verification link
   * @throws MessagingException if an error occurs while sending the email
   */
  @GetMapping("/request-password-reset")
  public ResponseEntity<MessageResponse> processForgotPassword(@RequestParam String email, HttpServletRequest request)
      throws MessagingException {
    try {
      UUID uuid = UUID.randomUUID();
      String token = uuid.toString().replaceAll("-", "");

      Optional<User> optionalUser = userRepository.findByEmail(email);

      if (!optionalUser.isPresent()) {
        return ResponseEntity.badRequest().body(new MessageResponse("User not found",  "error"));
      }

      User user = optionalUser.get();

      if (user == null) {
        return ResponseEntity.ok()
            .body(new MessageResponse("We have sent you a reset link. Please check your email.",  "success"));
      }

      user.setPasswordResetToken(token);
      user.setPasswordResetTokenExpiryDate(LocalDateTime.now().plusHours(2));
      userRepository.save(user);

      String userEmail = user.getEmail();

      // Determine the base URL based on the Origin header
      String baseUrl = clientInfoConfig.determineBaseUrl(request);

      String passwordResetLink = baseUrl + "/reset-password?email=" + userEmail + "&token=" + token;
      System.out.println(passwordResetLink);

      sendResetEmail(email, user.getUsername(), passwordResetLink);

      return ResponseEntity.ok()
          .body(new MessageResponse("We have sent you a reset link. Please check your email.",  "success"));

    } catch (MessagingException exception) {
      return ResponseEntity.status(500).body(new MessageResponse("Error while sending email",  "error"));
    }
  }

  /**
   * Resets the password of the user.
   * 
   * @param resetPasswordRequest
   * @throws MessagingException if an error occurs while sending the email
   */
  @PostMapping("/reset-password")
  public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest)
      throws MessagingException {

    String email = resetPasswordRequest.getEmail();
    String passwordResetToken = resetPasswordRequest.getPasswordResetToken();
    String newPassword = resetPasswordRequest.getPassword();
    String newPasswordCheck = resetPasswordRequest.getPasswordCheck();

    Optional<User> optionalUser = userRepository.findByEmailAndPasswordResetToken(email, passwordResetToken);

    if (!optionalUser.isPresent()) {
      return ResponseEntity.badRequest().body(new MessageResponse("Invalid token",  "error"));
    }

    User user = optionalUser.get();

    if (!newPassword.equals(newPasswordCheck)) {
      return ResponseEntity.badRequest().body(new MessageResponse("Passwords do not match",  "error"));
    }

    BCryptPasswordEncoder bc = new BCryptPasswordEncoder();
    String hashPwd = bc.encode(newPassword);

    user.setPasswordHash(hashPwd);
    user.setPasswordResetToken(null);
    user.setPasswordResetTokenExpiryDate(null);

    userRepository.save(user);

    return ResponseEntity.ok().body(new MessageResponse("Password reset successfully",  "success"));
  }

  /**
   * Sends an email with a verification link to the user.
   * 
   * @param toEmail         the email address of the user
   * @param username        the username of the user
   * @param confirmationUrl the verification link
   * @throws MessagingException if an error occurs while sending the email
   */
  private void sendVerificationEmail(String toEmail, String username, String confirmationUrl)
      throws MessagingException {
    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message);

    String fromEmail = dotenv.get("SPRING_MAIL_EMAIL");
    helper.setFrom(fromEmail);
    helper.setTo(toEmail);

    String content = "<div style=\"font-family: Arial, sans-serif; line-height: 1.6;\">" +
        "  <h2>Email Confirmation Required</h2>" +
        "  <p>Dear " + username + ",</p>" +
        "  <p>Thank you for registering with our service. To complete your registration, please confirm your email address by clicking the button below:</p>"
        +
        "  <p><a href=\"" + confirmationUrl
        + "\" style=\"display: inline-block; padding: 10px 20px; font-size: 16px; color: white; background-color: blue; text-decoration: none; border-radius: 5px;\">Confirm Email</a></p>"
        +
        "  <p>If you did not request this email, please disregard it. No further action is required on your part.</p>" +
        "  <p>Best regards,<br>My budget Pal Team</p>" +
        "</div>";

    helper.setSubject("Email Verification");
    helper.setText(content, true);

    mailSender.send(message);
  }

  /**
   * Sends an email with a password reset link to the user.
   *
   * @param toEmail           the email address of the user
   * @param username          the username of the user
   * @param passwordResetLink the password reset link
   * @throws MessagingException if an error occurs while sending the email
   */
  private void sendResetEmail(String toEmail, String username, String passwordResetLink) throws MessagingException {
    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message);

    String fromEmail = dotenv.get("SPRING_MAIL_EMAIL");
    helper.setFrom(fromEmail);
    helper.setTo(toEmail);

    String content = "<div style=\"font-family: Arial, sans-serif; line-height: 1.6;\">" +
        "  <p>Hello, " + username + "</p>" +
        "  <p>You have requested to reset your password. To proceed with resetting your password, please click the link below:</p>"
        +
        "  <p><a href=\"" + passwordResetLink
        + "\" style=\"display: inline-block; padding: 10px 20px; font-size: 16px; color: white; background-color: blue; text-decoration: none; border-radius: 5px;\">Change My Password</a></p>"
        +
        "  <p>If you did not request a password reset, please disregard this email. No further action is required on your part.</p>"
        +
        "  <p>Best regards,<br>My Pudget Pal Team</p>" +
        "</div>";

    helper.setSubject("Password reset link");
    helper.setText(content, true);

    mailSender.send(message);
  }
}