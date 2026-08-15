package hei.school.minou.service.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.school.minou.endpoint.rest.controller.dto.LoginRequest;
import hei.school.minou.endpoint.rest.controller.dto.LoginResponse;
import hei.school.minou.entity.User;
import hei.school.minou.entity.enums.Role;
import hei.school.minou.exception.BadRequestException;
import hei.school.minou.mapper.UserMapper;
import hei.school.minou.repository.UserRepository;
import hei.school.minou.repository.model.JUser;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private UserMapper userMapper;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private JwtService jwtService;

  private AuthService authService;

  @BeforeEach
  void setUp() {
    authService = new AuthService(userRepository, userMapper, passwordEncoder, jwtService);
  }

  @Test
  void login_withValidCredentials_returnsTokenAndUser() {
    UUID userId = UUID.randomUUID();
    String email = "admin@hei.school";
    String password = "secret";
    JUser jUser = jUser(userId, email, password);
    User user = User.builder().id(userId).email(email).role(Role.ADMIN).build();

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(jUser));
    when(passwordEncoder.matches(password, password)).thenReturn(true);
    when(userMapper.toDomain(jUser)).thenReturn(user);
    when(jwtService.generateToken(user)).thenReturn("fake-jwt-token");

    LoginResponse response = authService.login(new LoginRequest(email, password));

    assertThat(response.token()).isEqualTo("fake-jwt-token");
    assertThat(response.user()).isEqualTo(user);
    verify(userRepository).findByEmail(email);
    verify(passwordEncoder).matches(password, password);
    verify(jwtService).generateToken(user);
  }

  @Test
  void login_withUnknownEmail_throwsBadRequest() {
    String email = "ghost@hei.school";
    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.login(new LoginRequest(email, "secret")))
        .isInstanceOf(BadRequestException.class)
        .hasMessage("Invalid email or password");
  }

  @Test
  void login_withWrongPassword_throwsBadRequest() {
    String email = "admin@hei.school";
    JUser jUser = jUser(UUID.randomUUID(), email, "correct-password");

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(jUser));
    when(passwordEncoder.matches("wrong-password", "correct-password")).thenReturn(false);

    assertThatThrownBy(() -> authService.login(new LoginRequest(email, "wrong-password")))
        .isInstanceOf(BadRequestException.class)
        .hasMessage("Invalid email or password");
  }

  private static JUser jUser(UUID id, String email, String password) {
    return new JUser(id, "Admin", "Minou", Role.ADMIN, email, password, null);
  }
}
