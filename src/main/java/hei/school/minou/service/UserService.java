package hei.school.minou.service;

import hei.school.minou.endpoint.rest.controller.dto.UpdateUserRequest;
import hei.school.minou.entity.User;
import hei.school.minou.entity.enums.Role;
import hei.school.minou.exception.ResourceNotFoundException;
import hei.school.minou.mapper.UserMapper;
import hei.school.minou.repository.PromotionRepository;
import hei.school.minou.repository.UserRepository;
import hei.school.minou.repository.model.JPromotion;
import hei.school.minou.repository.model.JUser;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PromotionRepository promotionRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;

  public User saveUser(User user) {
    UUID id = user.id() != null ? user.id() : UUID.randomUUID();
    User encoded =
        User.builder()
            .id(id)
            .firstName(user.firstName())
            .lastName(user.lastName())
            .role(user.role())
            .email(user.email())
            .password(passwordEncoder.encode(user.password()))
            .promotion(user.promotion())
            .build();
    return userMapper.toDomain(userRepository.save(userMapper.toJpa(encoded)));
  }

  @Transactional
  public User updateUser(UUID id, UpdateUserRequest request) {
    JUser jUser =
        userRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    if (request.firstName() != null) {
      jUser.setFirstName(request.firstName());
    }
    if (request.lastName() != null) {
      jUser.setLastName(request.lastName());
    }
    if (request.role() != null) {
      jUser.setRole(request.role());
    }
    if (request.email() != null) {
      jUser.setEmail(request.email());
    }
    if (request.password() != null && !request.password().isBlank()) {
      jUser.setPassword(passwordEncoder.encode(request.password()));
    }
    if (request.promotionId() != null) {
      JPromotion promotion =
          promotionRepository
              .findById(request.promotionId())
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "Promotion not found: " + request.promotionId()));
      jUser.setPromotion(promotion);
    }
    return userMapper.toDomain(userRepository.save(jUser));
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
