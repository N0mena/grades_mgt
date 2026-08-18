package hei.school.minou.service.auth;

import hei.school.minou.endpoint.rest.controller.dto.LoginRequest;
import hei.school.minou.endpoint.rest.controller.dto.LoginResponse;
import hei.school.minou.exception.BadRequestException;
import org.springframework.stereotype.Service;

@Service
public class LoginViewService {

    private final AuthService authService;

    public LoginViewService(AuthService authService) {
        this.authService = authService;
    }

    public LoginResponse login(String email, String password) {
        try {
            LoginRequest request = LoginRequest.builder().email(email).password(password).build();
            LoginResponse response = authService.login(request);
            return LoginResponse.success(response.token());
        } catch (BadRequestException e) {
            return LoginResponse.failure("Email ou mot de passe invalide");
        }
    }
}