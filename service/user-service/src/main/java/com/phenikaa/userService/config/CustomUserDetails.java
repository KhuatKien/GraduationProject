//package com.phenikaa.userService.config;
//
//import com.phenikaa.userService.entity.User;
//import lombok.Getter;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//
//import java.util.Collection;
//
//@Getter
//public class CustomUserDetails implements UserDetails {
//    private final Integer userId;
//    private final String username;
//    private final String password;
//    private final Collection<? extends GrantedAuthority> authorities;
//
//    public CustomUserDetails(User user, Collection<? extends GrantedAuthority> authorities) {
//        this.userId = user.getUserId();
//        this.username = user.getUserName();
//        this.password = user.getPassword();
//        this.authorities = authorities;
//    }
//
//    @Override public boolean isAccountNonExpired() { return true; }
//    @Override public boolean isAccountNonLocked() { return true; }
//    @Override public boolean isCredentialsNonExpired() { return true; }
//    @Override public boolean isEnabled() { return true; }
//}
