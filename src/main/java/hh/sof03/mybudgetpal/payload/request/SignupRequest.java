package hh.sof03.mybudgetpal.payload.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

public class SignupRequest {
  @NotBlank(message="Username is mandatory")
  @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
  @Size(min=2, max=30)
  private String username = "";

  @NotBlank(message="First name is mandatory")
  @Size(min=2, max=320)
  private String firstName = "";

  @NotBlank(message="Last name is mandatory")
  @Size(min=2, max=320)
  private String lastName = "";

  @NotBlank(message="Email is mandatory")
  @Size(min=3, max=320)
  @Pattern(regexp=".+@.+\\..+", message="Email address must be valid")
  private String email = "";

  @NotBlank(message="Password is mandatory")
  @Size(min=8)
  @Pattern(regexp="^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", message="Password must contain at least one uppercase letter, one lowercase letter, and one number")
  private String password = "";

  @NotBlank(message="Password check is mandatory")
  @Size(min=8)
  private String passwordCheck = "";

  public SignupRequest() {
  }

  public SignupRequest(String username, String firstName, String lastName, String email, String password, String passwordCheck) {
    super();
    this.username = username;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.password = password;
    this.passwordCheck = passwordCheck;
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
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPasswordCheck() {
		return passwordCheck;
	}

  public void setPasswordCheck(String passwordCheck) {
    this.passwordCheck = passwordCheck;
  }

}
