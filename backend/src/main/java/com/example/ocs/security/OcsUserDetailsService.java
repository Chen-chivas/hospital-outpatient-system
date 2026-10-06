package com.example.ocs.security;

import com.example.ocs.module.user.infra.UserRepository;
import java.util.stream.Collectors;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class OcsUserDetailsService implements UserDetailsService {
  private final UserRepository userRepository;

  public OcsUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    var user = userRepository.findByUsernameIgnoreCase(username)
        .orElseThrow(() -> new UsernameNotFoundException("user not found"));
    if (!user.isActive()) {
      throw new UsernameNotFoundException("user disabled");
    }

    var authorities = user.getRoles().stream()
        .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getCode()))
        .collect(Collectors.toSet());

    return User.withUsername(user.getUsername())
        .password(user.getPasswordHash())
        .authorities(authorities)
        .build();
  }
}

