package hh.sof03.mybudgetpal.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


import java.time.LocalDateTime;
import java.util.Set;

@Document(collection = "users")
public class User {

  @Id
  private String id;

  @NotBlank(message = "Username is mandatory")
  @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
  @Size(min = 2, max = 30, message = "Username must be between 3 and 30 characters")
  @Indexed(unique = true) 
  private String username;

  @NotBlank(message = "First name is mandatory")
  @Pattern(regexp = "^[a-zA-Z]+$", message = "First name can only contain letters")
  private String firstName;

  @NotBlank(message = "Last name is mandatory")
  @Pattern(regexp = "^[a-zA-Z]+$", message = "Last name can only contain letters")
  private String lastName;

  @Email(message = "Email address must be valid")
  @Indexed(unique = true)
  private String email;

  private String passwordHash;
 
  private Set<String> roles;

  private boolean enabled;

  private String passwordResetToken;

  private LocalDateTime passwordResetTokenExpiryDate;

  private String emailVerificationToken;

  private LocalDateTime emailVerifcationTokenExpiryDate;

  public User() {
  }

  public User(String username, String firstName, String lastName, String email, String passwordHash) {
    super();
    this.username = username;
    this.firstName = firstName.toLowerCase();
    this.lastName = lastName.toLowerCase();
    this.email = email;
    this.passwordHash = passwordHash;
  }

  public String getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    firstName = firstName.toLowerCase();
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    lastName = lastName.toLowerCase();
    this.lastName = lastName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public Set<String> getRoles() {
    return roles;
  }

  public void setRoles(Set<String> roles) {
    this.roles = roles;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getPasswordResetToken() {
    return passwordResetToken;
  }

  public void setPasswordResetToken(String passwordResetToken) {
    this.passwordResetToken = passwordResetToken;
  }

  public LocalDateTime getPasswordResetTokenExpiryDate() {
    return passwordResetTokenExpiryDate;
  }

  public void setPasswordResetTokenExpiryDate(LocalDateTime passwordResetTokenExpiryDate) {
    this.passwordResetTokenExpiryDate = passwordResetTokenExpiryDate;
  }

  public String getEmailVerificationToken() {
    return emailVerificationToken;
  }

  public void setEmailVerificationToken(String emailVerificationToken) {
    this.emailVerificationToken = emailVerificationToken;
  }

  public LocalDateTime getEmailVerificationTokenExpiryDate() {
    return emailVerifcationTokenExpiryDate;
  }

  public void setEmailVerificationTokenExpiryDate(LocalDateTime emailVerifcationTokenExpiryDate) {
    this.emailVerifcationTokenExpiryDate = emailVerifcationTokenExpiryDate;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    User user = (User) o;

    return id != null ? id.equals(user.id) : user.id == null;
  }

}