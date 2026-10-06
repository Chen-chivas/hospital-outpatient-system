package com.example.ocs.module.user.application;

import com.example.ocs.common.exception.BusinessException;
import com.example.ocs.common.web.ErrorCode;
import com.example.ocs.module.user.api.CreateUserRequest;
import com.example.ocs.module.user.domain.PatientType;
import com.example.ocs.module.user.domain.User;
import com.example.ocs.module.user.domain.UserStatus;
import com.example.ocs.module.user.infra.RoleRepository;
import com.example.ocs.module.user.infra.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public User create(CreateUserRequest request) {
    if (userRepository.existsByUsernameIgnoreCase(request.username())) {
      throw new BusinessException(ErrorCode.CONFLICT, "username already exists");
    }

    Instant now = Instant.now();
    User user = new User(
        request.username(),
        passwordEncoder.encode(request.password()),
        request.displayName(),
        UserStatus.ACTIVE,
        now
    );
    Set<String> roleCodes = request.roles() == null ? Set.of() : Set.copyOf(request.roles());
    if (!roleCodes.isEmpty()) {
      var roles = roleCodes.stream()
          .map(code -> roleRepository.findByCode(code)
              .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "role not found: " + code)))
          .collect(java.util.stream.Collectors.toSet());
      user.getRoles().addAll(roles);
    }
    if (request.patientType() != null) {
      user.setPatientType(request.patientType());
    } else if (roleCodes.contains("PATIENT")) {
      user.setPatientType(PatientType.SELF_PAY);
    }

    return userRepository.save(user);
  }

  public List<User> list() {
    return userRepository.findAll();
  }

  public User getById(long id) {
    return userRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "user not found"));
  }

  @Transactional
  public User updateStatus(long id, UserStatus status) {
    User user = getById(id);
    user.setStatus(status);
    return userRepository.save(user);
  }

  @Transactional
  public User resetPassword(long id, String newPassword) {
    User user = getById(id);
    String hash = passwordEncoder.encode(newPassword);
    user.setPasswordHash(hash);
    return userRepository.save(user);
  }
}
