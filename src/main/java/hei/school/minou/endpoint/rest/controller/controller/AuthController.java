package hei.school.minou.endpoint.rest.controller.controller;

import hei.school.minou.endpoint.rest.controller.dto.LoginRequest;
import hei.school.minou.endpoint.rest.controller.dto.LoginResponse;
import hei.school.minou.service.auth.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
public class  AuthController {

  private final AuthService authService;

  @PostMapping("/auth/login")
  public LoginResponse login(@RequestBody LoginRequest request) {
    return authService.login(request);
  }
}
