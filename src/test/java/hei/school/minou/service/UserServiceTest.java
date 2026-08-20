package hei.school.minou.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.school.minou.entity.User;
import hei.school.minou.entity.enums.Role;
import hei.school.minou.exception.ResourceNotFoundException;
import hei.school.minou.mapper.UserMapper;
import hei.school.minou.repository.PromotionRepository;
import hei.school.minou.repository.UserRepository;
import hei.school.minou.repository.model.JUser;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class UserServiceTest {

  private UserRepository userRepository;
  private PromotionRepository promotionRepository;
  private UserMapper userMapper;
  private PasswordEncoder passwordEncoder;
  private UserService userService;

  @BeforeEach
  void setUp() {
    userRepository = mock(UserRepository.class);
    promotionRepository = mock(PromotionRepository.class);
    userMapper = mock(UserMapper.class);
    passwordEncoder = mock(PasswordEncoder.class);
    userService = new UserService(userRepository, promotionRepository, userMapper, passwordEncoder);
  }

  @Nested
  class SaveUser {

    @Test
    void encodesPasswordAndSaves() {
      User user =
          User.builder()
              .id(UUID.randomUUID())
              .firstName("A")
              .lastName("M")
              .role(Role.STUDENT)
              .email("a@m.h")
              .password("plain")
              .build();
      JUser jUser = new JUser(user.id(), "A", "M", Role.STUDENT, "a@m.h", "encoded", null);
      when(passwordEncoder.encode("plain")).thenReturn("encoded");
      when(userMapper.toJpa(any(User.class))).thenReturn(jUser);
      when(userRepository.save(jUser)).thenReturn(jUser);
      User saved = User.builder().id(user.id()).password("encoded").build();
      when(userMapper.toDomain(jUser)).thenReturn(saved);

      User result = userService.saveUser(user);

      assertThat(result.password()).isEqualTo("encoded");
      verify(passwordEncoder).encode("plain");
      verify(userRepository).save(jUser);
    }
  }

  @Nested
  class GetUserById {

    @Test
    void unknownUser_throwsNotFound() {
      UUID id = UUID.randomUUID();
      when(userRepository.findById(id)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> userService.getUserById(id))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessage("User not found: " + id);
    }

    @Test
    void knownUser_isReturned() {
      UUID id = UUID.randomUUID();
      JUser jUser = new JUser(id, "A", "M", Role.STUDENT, "a@m.h", "enc", null);
      User expected = User.builder().id(id).build();
      when(userRepository.findById(id)).thenReturn(Optional.of(jUser));
      when(userMapper.toDomain(jUser)).thenReturn(expected);

      assertThat(userService.getUserById(id)).isEqualTo(expected);
    }
  }

  @Nested
  class GetUserByEmail {

    @Test
    void unknownEmail_throwsNotFound() {
      when(userRepository.findByEmail("x@y.z")).thenReturn(Optional.empty());

      assertThatThrownBy(() -> userService.getUserByEmail("x@y.z"))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessage("User not found with email: x@y.z");
    }

    @Test
    void knownEmail_isReturned() {
      UUID id = UUID.randomUUID();
      JUser jUser = new JUser(id, "A", "M", Role.STUDENT, "a@m.h", "enc", null);
      User expected = User.builder().id(id).build();
      when(userRepository.findByEmail("a@m.h")).thenReturn(Optional.of(jUser));
      when(userMapper.toDomain(jUser)).thenReturn(expected);

      assertThat(userService.getUserByEmail("a@m.h")).isEqualTo(expected);
    }
  }

  @Nested
  class GetAllUsers {

    @Test
    void returnsAllMappedUsers() {
      JUser jUser = new JUser(UUID.randomUUID(), "A", "M", Role.STUDENT, "a@m.h", "enc", null);
      when(userRepository.findAll()).thenReturn(List.of(jUser));
      when(userMapper.toDomain(jUser)).thenReturn(User.builder().id(jUser.getId()).build());

      List<User> users = userService.getAllUsers();

      assertThat(users).hasSize(1);
    }
  }

  @Nested
  class GetUsersByRole {

    @Test
    void returnsFilteredUsers() {
      JUser jUser = new JUser(UUID.randomUUID(), "A", "M", Role.TEACHER, "t@m.h", "enc", null);
      when(userRepository.findByRole(Role.TEACHER)).thenReturn(List.of(jUser));
      when(userMapper.toDomain(jUser))
          .thenReturn(User.builder().id(jUser.getId()).role(Role.TEACHER).build());

      List<User> users = userService.getUsersByRole(Role.TEACHER);

      assertThat(users).hasSize(1);
      assertThat(users.get(0).role()).isEqualTo(Role.TEACHER);
    }
  }
}
