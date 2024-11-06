package hh.sof03.mybudgetpal.payload.response;

import jakarta.validation.constraints.Pattern;

public class MessageResponse {
  @Pattern(regexp = "success|error")
  private String type;

  private String message;

  public MessageResponse() {
  }

  public MessageResponse(String message, String type) {
    this.message = message;
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}