package hh.sof03.mybudgetpal.payload.request;

import jakarta.validation.constraints.Pattern;

public class UserEmailTokenRequest {
    @Pattern(regexp = ".+@.+\\..+", message = "Email address must be valid")
    private String email = "";
    
    private String emailVerificationToken = "";
    private String passwordResetToken = "";

    public UserEmailTokenRequest() {
    }

    public UserEmailTokenRequest(String email, String emailVerificationToken, String passwordResetToken) {
        super();
        this.email = email;
        this.emailVerificationToken = emailVerificationToken;
        this.passwordResetToken = passwordResetToken;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmailVerificationToken() {
        return emailVerificationToken;
    }

    public void setEmailVerificationToken(String emailVerificationToken) {
        this.emailVerificationToken = emailVerificationToken;
    }

    public String getPasswordResetToken() {
        return passwordResetToken;
    }

    public void setPasswordResetToken(String passwordResetToken) {
        this.passwordResetToken = passwordResetToken;
    }
}
