package hei.school.minou.service;

import hei.school.minou.entity.User;
import hei.school.minou.entity.enums.Role;
import hei.school.minou.exception.ResourceNotFoundException;
import hei.school.minou.mapper.UserMapper;
import hei.school.minou.repository.UserRepository;
import hei.school.minou.repository.model.JUser;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  public User saveUser(User user) {
    return userMapper.toDomain(userRepository.save(userMapper.toJpa(user)));
  }

  public User getUserById(UUID id) {
    JUser jUser =
        userRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    return userMapper.toDomain(jUser);
  }

  public User getUserByEmail(String email) {
    JUser jUser =
        userRepository
            .findByEmail(email)
            .orElseThrow(
                () -> new ResourceNotFoundException("User not found with email: " + email));
    return userMapper.toDomain(jUser);
  }

  public List<User> getAllUsers() {
    return userRepository.findAll().stream().map(userMapper::toDomain).toList();
  }

  public List<User> getUsersByRole(Role role) {
    return userRepository.findByRole(role).stream().map(userMapper::toDomain).toList();
  }
}
