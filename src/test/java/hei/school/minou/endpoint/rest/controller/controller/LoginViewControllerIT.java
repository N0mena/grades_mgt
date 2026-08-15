package hei.school.minou.endpoint.rest.controller.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LoginViewControllerIT {

  private final LoginViewController loginViewController = new LoginViewController();

  @Test
  void showLoginPage_returnsLoginView() {
    assertThat(loginViewController.showLoginPage()).isEqualTo("login");
  }
}
