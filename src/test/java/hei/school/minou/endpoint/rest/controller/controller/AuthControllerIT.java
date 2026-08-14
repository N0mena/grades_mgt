package hei.school.minou.endpoint.rest.controller.controller;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

import hei.school.minou.endpoint.rest.controller.dto.LoginRequest;
import hei.school.minou.endpoint.rest.controller.dto.LoginResponse;
import hei.school.minou.entity.User;
import hei.school.minou.service.auth.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AuthControllerIT {
  @Mock private AuthService authService;

  private AuthController authController;

  @BeforeEach
  void setUp() {
    authController = new AuthController(authService);
  }

  @Test
  void should_delegate_login_to_auth_service_and_return_response() {

    LoginRequest request = LoginRequest.builder().email("nomena@hei.school").password("secret").build();
    User user = mock(User.class);
    LoginResponse expectedResponse =
            LoginResponse.builder().token("fake-jwt-token").user(user).build();

    when(authService.login(request)).thenReturn(expectedResponse);

    LoginResponse result = authController.login(request);

    assertThat(result).isEqualTo(expectedResponse);
    assertThat(result.token()).isEqualTo("fake-jwt-token");
    assertThat(result.user()).isEqualTo(user);
    verify(authService).login(request);
  }
}
