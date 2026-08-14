package hei.school.minou.service.auth;

import hei.school.minou.endpoint.rest.controller.dto.LoginRequest;
import hei.school.minou.endpoint.rest.controller.dto.LoginResponse;
import hei.school.minou.entity.User;
import hei.school.minou.mapper.UserMapper;
import hei.school.minou.repository.UserRepository;
import hei.school.minou.repository.model.JUser;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public LoginResponse login(LoginRequest request) {
    JUser jUser =
        userRepository
            .findByEmail(request.email())
            .orElseThrow(() -> new RuntimeException("Invalid email or password"));
    if (!passwordEncoder.matches(request.password(), jUser.getPassword())) {
      throw new RuntimeException("Invalid email or password");
    }
    User user = userMapper.toDomain(jUser);
    return LoginResponse.builder().token(jwtService.generateToken(user)).user(user).build();
  }
}
