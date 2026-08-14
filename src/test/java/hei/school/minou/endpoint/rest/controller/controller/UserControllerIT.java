package hei.school.minou.endpoint.rest.controller.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.school.minou.entity.User;
import hei.school.minou.entity.enums.Role;
import hei.school.minou.security.SecurityUtils;
import hei.school.minou.service.UserService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserControllerIT {

  @Mock private UserService userService;
  @Mock private SecurityUtils securityUtils;

  private UserController userController;

  @BeforeEach
  void setUp() {
    userController = new UserController(userService, securityUtils);
  }

  @Test
  void should_return_all_users() {
    User user = mock(User.class);
    when(userService.getAllUsers()).thenReturn(List.of(user));

    List<User> result = userController.getAllUsers();

    assertThat(result).containsExactly(user);
    verify(userService).getAllUsers();
  }

  @Test
  void should_return_current_user() {
    User currentUser = mock(User.class);
    when(securityUtils.currentUser()).thenReturn(currentUser);

    User result = userController.getCurrentUser();

    assertThat(result).isEqualTo(currentUser);
    verify(securityUtils).currentUser();
  }

  @Test
  void should_return_user_by_id() {
    UUID userId = UUID.randomUUID();
    User user = mock(User.class);
    when(userService.getUserById(userId)).thenReturn(user);

    User result = userController.getUserById(userId);

    assertThat(result).isEqualTo(user);
    verify(userService).getUserById(userId);
  }

  @Test
  void should_return_users_by_role() {
    User user = mock(User.class);
    when(userService.getUsersByRole(Role.STUDENT)).thenReturn(List.of(user));

    List<User> result = userController.getUsersByRole(Role.STUDENT);

    assertThat(result).containsExactly(user);
    verify(userService).getUsersByRole(Role.STUDENT);
  }

  @Test
  void should_create_user() {
    User userToCreate = mock(User.class);
    User savedUser = mock(User.class);
    when(userService.saveUser(userToCreate)).thenReturn(savedUser);

    User result = userController.createUser(userToCreate);

    assertThat(result).isEqualTo(savedUser);
    verify(userService).saveUser(userToCreate);
  }
}
