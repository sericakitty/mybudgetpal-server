package hh.sof03.mybudgetpal.security.services;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

public class CustomUserDetails implements UserDetails {
    private static final long serialVersionUID = 1L;
    private final String id;
    private final String username;
    private final String email;

    @JsonIgnore
    private final String password;
    
    private final List<GrantedAuthority> authorities;

    public CustomUserDetails(String id, String username, String email, String password, List<GrantedAuthority> authorities) {
        this.id = id;  
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    @Override
    public List<GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public String getId() {
        return id;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
