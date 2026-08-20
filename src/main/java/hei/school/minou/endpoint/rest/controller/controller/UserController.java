package hei.school.minou.endpoint.rest.controller.controller;

import hei.school.minou.endpoint.rest.controller.dto.UpdateUserRequest;
import hei.school.minou.entity.User;
import hei.school.minou.entity.enums.Role;
import hei.school.minou.security.SecurityUtils;
import hei.school.minou.service.UserService;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class UserController {

  private final UserService userService;
  private final SecurityUtils securityUtils;

  @GetMapping("/users")
  public List<User> getAllUsers() {
    return userService.getAllUsers();
  }

  @GetMapping("/users/me")
  public User getCurrentUser() {
    return securityUtils.currentUser();
  }

  @GetMapping("/users/{id}")
  public User getUserById(@PathVariable UUID id) {
    return userService.getUserById(id);
  }

  @GetMapping("/users/role/{role}")
  public List<User> getUsersByRole(@PathVariable Role role) {
    return userService.getUsersByRole(role);
  }

  @PostMapping("/users")
  public User createUser(@RequestBody User user) {
    return userService.saveUser(user);
  }

  @PutMapping("/users/{id}")
  public User updateUser(@PathVariable UUID id, @RequestBody UpdateUserRequest request) {
    return userService.updateUser(id, request);
  }
}
