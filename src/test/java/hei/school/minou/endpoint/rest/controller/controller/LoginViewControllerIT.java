package hei.school.minou.endpoint.rest.controller.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import hei.school.minou.endpoint.rest.controller.dto.LoginRequest;
import hei.school.minou.endpoint.rest.controller.dto.LoginResponse;
import hei.school.minou.exception.BadRequestException;
import hei.school.minou.service.auth.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

class LoginViewControllerIT {

  private AuthService authService;
  private LoginViewController loginViewController;

  @BeforeEach
  void setUp() {
    authService = mock(AuthService.class);
    loginViewController = new LoginViewController(authService);
  }

  @Test
  void showLoginPage_returnsLoginView() {
    assertThat(loginViewController.showLoginPage()).isEqualTo("login");
  }

  @Test
  void login_withValidCredentials_setsCookieAndRedirects() {
    LoginResponse loginResponse = LoginResponse.builder().token("fake-jwt-token").build();
    when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

    HttpServletResponse response = new MockHttpServletResponse();
    Model model = new ExtendedModelMap();

    String view = loginViewController.login("test.admin@hei.school", "Admin123!", response, model);

    assertThat(view).isEqualTo("redirect:/ui/promotions");
    assertThat(((MockHttpServletResponse) response).getCookie("access_token")).isNotNull();
    assertThat(((MockHttpServletResponse) response).getCookie("access_token").getValue())
        .isEqualTo("fake-jwt-token");
  }

  @Test
  void login_withInvalidCredentials_returnsLoginViewWithError() {
    when(authService.login(any(LoginRequest.class)))
        .thenThrow(new BadRequestException("Invalid email or password"));

    HttpServletResponse response = new MockHttpServletResponse();
    Model model = new ExtendedModelMap();

    String view = loginViewController.login("wrong@hei.school", "wrongpass", response, model);

    assertThat(view).isEqualTo("login");
    assertThat(model.getAttribute("error")).isEqualTo("Email ou mot de passe invalide");
  }
}
