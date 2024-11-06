
package hh.sof03.mybudgetpal.payload.request;

import jakarta.validation.constraints.NotEmpty;

public class LoginRequest {
  
  @NotEmpty
  private String identifier; // can be either email or username

  @NotEmpty
  private String password;

  public LoginRequest() {
  }

  public LoginRequest(String identifier, String password) {
    super();
    this.identifier = identifier;
    this.password = password;
  }

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
