package hei.school.minou.endpoint.rest.controller.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import hei.school.minou.endpoint.rest.controller.dto.LoginResponse;
import hei.school.minou.service.auth.LoginViewService;
import hei.school.minou.service.url.UrlService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

class LoginViewControllerIT {

  private LoginViewService loginViewService;
  private UrlService urlService;
  private LoginViewController loginViewController;

  @BeforeEach
  void setUp() {
    loginViewService = mock(LoginViewService.class);
    urlService = mock(UrlService.class);
    loginViewController = new LoginViewController(loginViewService, urlService);
  }

  @Test
  void showLoginPage_returnsLoginView() {
    assertThat(loginViewController.showLoginPage()).isEqualTo("login");
  }

  @Test
  void login_withValidCredentials_setsCookieAndRedirects() {
    Cookie expectedCookie = new Cookie("access_token", "fake-jwt-token");
    expectedCookie.setHttpOnly(true);
    expectedCookie.setPath("/");
    expectedCookie.setMaxAge(2 * 60 * 60);

    when(loginViewService.login("test.admin@hei.school", "Admin123!"))
        .thenReturn(LoginResponse.success("fake-jwt-token"));
    when(loginViewService.buildAuthCookie("fake-jwt-token")).thenReturn(expectedCookie);
    when(urlService.buildRedirectUrl("/ui/promotions"))
        .thenReturn("redirect:https://fake-url.on.aws/ui/promotions");

    HttpServletResponse response = new MockHttpServletResponse();
    Model model = new ExtendedModelMap();

    String view = loginViewController.login("test.admin@hei.school", "Admin123!", response, model);

    assertThat(view).isEqualTo("redirect:https://fake-url.on.aws/ui/promotions");
    assertThat(((MockHttpServletResponse) response).getCookie("access_token")).isNotNull();
    assertThat(((MockHttpServletResponse) response).getCookie("access_token").getValue())
        .isEqualTo("fake-jwt-token");
  }

  @Test
  void login_withInvalidCredentials_returnsLoginViewWithError() {
    when(loginViewService.login("wrong@hei.school", "wrongpass"))
        .thenReturn(LoginResponse.failure("Email ou mot de passe invalide"));

    HttpServletResponse response = new MockHttpServletResponse();
    Model model = new ExtendedModelMap();

    String view = loginViewController.login("wrong@hei.school", "wrongpass", response, model);

    assertThat(view).isEqualTo("login");
    assertThat(model.getAttribute("error")).isEqualTo("Email ou mot de passe invalide");
  }
}
