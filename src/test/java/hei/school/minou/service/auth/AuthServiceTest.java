package hei.school.minou.service.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private UserMapper userMapper;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private JwtService jwtService;

  @InjectMocks private AuthService authService;

  private JUser jUser;
  private User user;

  @BeforeEach
  void setUp() {
    UUID userId = UUID.randomUUID();

    jUser = new JUser();
    jUser.setId(userId);
    jUser.setEmail("nomena@hei.school");
    jUser.setPassword("hashedPassword");
    jUser.setRole(Role.ADMIN);

    user = User.builder().id(userId).email("nomena@hei.school").role(Role.ADMIN).build();
  }

  @Test
  void should_login_successfully_with_valid_credentials() {
    LoginRequest request =
        LoginRequest.builder().email("nomena@hei.school").password("secret").build();

    when(userRepository.findByEmail("nomena@hei.school")).thenReturn(Optional.of(jUser));
    when(passwordEncoder.matches("secret", "hashedPassword")).thenReturn(true);
    when(userMapper.toDomain(jUser)).thenReturn(user);
    when(jwtService.generateToken(user)).thenReturn("fake-jwt-token");

    LoginResponse response = authService.login(request);

    assertThat(response.token()).isEqualTo("fake-jwt-token");
    assertThat(response.user().email()).isEqualTo("nomena@hei.school");
  }

  @Test
  void should_throw_bad_request_when_email_not_found() {
    LoginRequest request =
        LoginRequest.builder().email("inconnu@hei.school").password("secret").build();

    when(userRepository.findByEmail("inconnu@hei.school")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.login(request))
        .isInstanceOf(BadRequestException.class)
        .hasMessage("Invalid email or password");
  }

  @Test
  void should_throw_bad_request_when_password_is_wrong() {
    LoginRequest request =
        LoginRequest.builder().email("nomena@hei.school").password("wrong").build();

    when(userRepository.findByEmail("nomena@hei.school")).thenReturn(Optional.of(jUser));
    when(passwordEncoder.matches("wrong", "hashedPassword")).thenReturn(false);

    assertThatThrownBy(() -> authService.login(request))
        .isInstanceOf(BadRequestException.class)
        .hasMessage("Invalid email or password");
  }
}
