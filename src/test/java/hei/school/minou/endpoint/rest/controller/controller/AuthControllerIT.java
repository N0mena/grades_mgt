package hei.school.minou.endpoint.rest.controller.controller;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

import hei.school.minou.endpoint.rest.controller.dto.LoginRequest;
import hei.school.minou.endpoint.rest.controller.dto.LoginResponse;
import hei.school.minou.service.auth.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

public class AuthControllerIT {
  @Mock private AuthService authService;

  private AuthController authController;

  @BeforeEach
  void setUp() {
    authController = new AuthController(authService);
  }

  @Test
  void should_delegate_login_to_auth_service_and_return_response() {
    LoginRequest request = mock(LoginRequest.class);
    LoginResponse expectedResponse = mock(LoginResponse.class);
    when(authService.login(request)).thenReturn(expectedResponse);

    LoginResponse result = authController.login(request);

    assertThat(result).isEqualTo(expectedResponse);
    verify(authService).login(request);
  }
}
