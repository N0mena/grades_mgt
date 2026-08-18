package hei.school.minou.endpoint.rest.controller.controller;

import hei.school.minou.endpoint.rest.controller.dto.LoginResponse;
import hei.school.minou.service.auth.LoginViewService;
import hei.school.minou.service.url.UrlService;
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

  private final LoginViewService loginViewService;
  private final UrlService urlService;

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

    LoginResponse result = loginViewService.login(email, password);

    if (!result.success()) {
      model.addAttribute("error", result.errorMessage());
      return "login";
    }

    Cookie cookie = loginViewService.buildAuthCookie(result.token());
    response.addCookie(cookie);

    return urlService.buildRedirectUrl("/ui/promotions");
  }
}
