package hei.school.minou.service.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import hei.school.minou.conf.JwtProperties;
import hei.school.minou.entity.User;
import hei.school.minou.entity.enums.Role;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

  private static final String SECRET = "change-me-change-me-change-me-change-me-123456";

  private JwtService jwtService;

  @BeforeEach
  void setUp() {
    jwtService = new JwtService(new JwtProperties(SECRET, 86_400_000L));
  }

  @Test
  void generateTokenThenParseToken_returnsSamePrincipal() {
    User user =
        User.builder()
            .id(UUID.randomUUID())
            .firstName("Alida")
            .lastName("Minou")
            .role(Role.TEACHER)
            .email("alida@hei.school")
            .build();

    String token = jwtService.generateToken(user);

    AuthPrincipal principal = jwtService.parseToken(token);
    assertThat(principal.userId()).isEqualTo(user.id());
    assertThat(principal.role()).isEqualTo(Role.TEACHER);
    assertThat(principal.email()).isEqualTo("alida@hei.school");
  }

  @Test
  void generateToken_embedAdminRole() {
    User admin =
        User.builder().id(UUID.randomUUID()).role(Role.ADMIN).email("admin@hei.school").build();

    String token = jwtService.generateToken(admin);
    AuthPrincipal principal = jwtService.parseToken(token);

    assertThat(principal.role()).isEqualTo(Role.ADMIN);
  }

  @Test
  void parseToken_withGarbageToken_throws() {
    assertThatThrownBy(() -> jwtService.parseToken("not-a-jwt"))
        .isInstanceOf(RuntimeException.class);
  }
}
