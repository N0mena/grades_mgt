package hei.school.minou.endpoint.rest.controller.controller;

import hei.school.minou.endpoint.rest.controller.dto.LoginRequest;
import hei.school.minou.endpoint.rest.controller.dto.LoginResponse;
import hei.school.minou.exception.BadRequestException;
import hei.school.minou.service.auth.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@AllArgsConstructor
public class LoginViewController {

  private final AuthService authService;

  @GetMapping("/login")
  public String showLoginPage() {
    return "login";
  }

  @PostMapping("/login")
  public String login(
      @RequestParam String email,
      @RequestParam String password,
      HttpServletResponse response,
      Model model) {
    try {
      LoginRequest request = LoginRequest.builder().email(email).password(password).build();
      LoginResponse loginResponse = authService.login(request);

      Cookie cookie = new Cookie("access_token", loginResponse.token());
      cookie.setHttpOnly(true);
      cookie.setPath("/");
      cookie.setMaxAge(2 * 60 * 60);
      response.addCookie(cookie);

      return "redirect:/ui/promotions";

    } catch (BadRequestException e) {
      model.addAttribute("error", "Email ou mot de passe invalide");
      return "login";
    }
  }
}
