package hei.school.minou.service.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import hei.school.minou.endpoint.rest.controller.dto.LoginRequest;
import hei.school.minou.endpoint.rest.controller.dto.LoginResponse;
import hei.school.minou.exception.BadRequestException;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoginViewServiceTest {

  private AuthService authService;
  private LoginViewService loginViewService;

  @BeforeEach
  void setUp() {
    authService = mock(AuthService.class);
    loginViewService = new LoginViewService(authService);
  }

  @Test
  void login_withValidCredentials_returnsSuccessResult() {
    LoginResponse response = LoginResponse.success("fake-jwt-token");
    when(authService.login(any(LoginRequest.class))).thenReturn(response);

    LoginResponse result = loginViewService.login("test.admin@hei.school", "Admin123!");

    assertThat(result.success()).isTrue();
    assertThat(result.token()).isEqualTo("fake-jwt-token");
  }

  @Test
  void login_withInvalidCredentials_returnsFailureResult() {
    when(authService.login(any(LoginRequest.class)))
        .thenThrow(new BadRequestException("Invalid email or password"));

    LoginResponse result = loginViewService.login("wrong@hei.school", "wrongpass");

    assertThat(result.success()).isFalse();
    assertThat(result.errorMessage()).isEqualTo("Email ou mot de passe invalide");
  }

  @Test
  void buildAuthCookie_returnsCookieWithExpectedProperties() {
    Cookie cookie = loginViewService.buildAuthCookie("fake-jwt-token");

    assertThat(cookie.getName()).isEqualTo("access_token");
    assertThat(cookie.getValue()).isEqualTo("fake-jwt-token");
    assertThat(cookie.isHttpOnly()).isTrue();
    assertThat(cookie.getPath()).isEqualTo("/");
    assertThat(cookie.getMaxAge()).isEqualTo(2 * 60 * 60);
  }
}
