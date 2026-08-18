package hei.school.minou.service.auth;

import hei.school.minou.endpoint.rest.controller.dto.LoginRequest;
import hei.school.minou.endpoint.rest.controller.dto.LoginResponse;
import hei.school.minou.exception.BadRequestException;
import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Service;

@Service
public class LoginViewService {

  private static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";
  private static final int ACCESS_TOKEN_MAX_AGE_SECONDS = 2 * 60 * 60;

  private final AuthService authService;

  public LoginViewService(AuthService authService) {
    this.authService = authService;
  }

  public LoginResponse login(String email, String password) {
    try {
      LoginRequest request = LoginRequest.builder().email(email).password(password).build();
      return authService.login(request);
    } catch (BadRequestException e) {
      return LoginResponse.failure("Email ou mot de passe invalide");
    }
  }

  public Cookie buildAuthCookie(String token) {
    Cookie cookie = new Cookie(ACCESS_TOKEN_COOKIE_NAME, token);
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    cookie.setMaxAge(ACCESS_TOKEN_MAX_AGE_SECONDS);
    return cookie;
  }
}
