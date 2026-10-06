package com.example.ocs.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtTokenService tokenService;

  public JwtAuthenticationFilter(JwtTokenService tokenService) {
    this.tokenService = tokenService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String header = request.getHeader("Authorization");
    if (!StringUtils.hasText(header) || !header.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = header.substring("Bearer ".length()).trim();
    try {
      DecodedJWT decoded = tokenService.verify(token);
      Long userId = decoded.getClaim("uid").asLong();
      List<String> roleCodes = decoded.getClaim("roles").asList(String.class);
      String username = decoded.getSubject();

      List<SimpleGrantedAuthority> authorities = roleCodes == null
          ? List.of()
          : roleCodes.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList();
      OcsPrincipal principal = new OcsPrincipal(userId == null ? -1L : userId, username, roleCodes);

      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(principal, null, authorities);
      authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(authentication);
    } catch (Exception ignored) {
      // Ignore invalid token and continue as unauthenticated.
    }

    filterChain.doFilter(request, response);
  }
}

