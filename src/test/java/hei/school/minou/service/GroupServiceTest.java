package hei.school.minou.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.school.minou.entity.Group;
import hei.school.minou.entity.GroupHistory;
import hei.school.minou.entity.User;
import hei.school.minou.entity.enums.Role;
import hei.school.minou.exception.ResourceNotFoundException;
import hei.school.minou.mapper.GroupHistoryMapper;
import hei.school.minou.mapper.GroupMapper;
import hei.school.minou.mapper.UserMapper;
import hei.school.minou.repository.GroupHistoryRepository;
import hei.school.minou.repository.GroupRepository;
import hei.school.minou.repository.UserRepository;
import hei.school.minou.repository.model.JGroup;
import hei.school.minou.repository.model.JGroupHistory;
import hei.school.minou.repository.model.JUser;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class GroupServiceTest {

  private GroupRepository groupRepository;
  private UserRepository userRepository;
  private GroupHistoryRepository groupHistoryRepository;
  private GroupMapper groupMapper;
  private GroupHistoryMapper groupHistoryMapper;
  private UserMapper userMapper;
  private GroupService groupService;

  @BeforeEach
  void setUp() {
    groupRepository = mock(GroupRepository.class);
    userRepository = mock(UserRepository.class);
    groupHistoryRepository = mock(GroupHistoryRepository.class);
    groupMapper = mock(GroupMapper.class);
    groupHistoryMapper = mock(GroupHistoryMapper.class);
    userMapper = mock(UserMapper.class);
    groupService =
        new GroupService(
            groupRepository,
            userRepository,
            groupHistoryRepository,
            groupMapper,
            groupHistoryMapper,
            userMapper);
  }

  @Nested
  class SaveGroup {

    @Test
    void savesAndReturnsGroup() {
      Group group = Group.builder().id(UUID.randomUUID()).ref("G1").build();
      JGroup jGroup = new JGroup(group.id(), "G1");
      when(groupMapper.toJpa(group)).thenReturn(jGroup);
      when(groupRepository.save(jGroup)).thenReturn(jGroup);
      when(groupMapper.toDomain(jGroup)).thenReturn(group);

      Group result = groupService.saveGroup(group);

      assertThat(result).isEqualTo(group);
      verify(groupRepository).save(jGroup);
    }
  }

  @Nested
  class GetGroupById {

    @Test
    void unknownGroup_throwsNotFound() {
      UUID id = UUID.randomUUID();
      when(groupRepository.findById(id)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> groupService.getGroupById(id))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessage("Group not found: " + id);
    }

    @Test
    void knownGroup_isReturned() {
      UUID id = UUID.randomUUID();
      JGroup jGroup = new JGroup(id, "G1");
      Group expected = Group.builder().id(id).ref("G1").build();
      when(groupRepository.findById(id)).thenReturn(Optional.of(jGroup));
      when(groupMapper.toDomain(jGroup)).thenReturn(expected);

      assertThat(groupService.getGroupById(id)).isEqualTo(expected);
    }
  }

  @Nested
  class GetAllGroups {

    @Test
    void returnsAllGroups() {
      JGroup jGroup = new JGroup(UUID.randomUUID(), "G1");
      when(groupRepository.findAll()).thenReturn(List.of(jGroup));
      when(groupMapper.toDomain(jGroup)).thenReturn(Group.builder().id(jGroup.getId()).build());

      List<Group> groups = groupService.getAllGroups();

      assertThat(groups).hasSize(1);
    }
  }

  @Nested
  class AssignStudent {

    @Test
    void unknownStudent_throwsNotFound() {
      UUID studentId = UUID.randomUUID();
      when(userRepository.findById(studentId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> groupService.assignStudent(studentId, UUID.randomUUID()))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessage("Student not found: " + studentId);
    }

    @Test
    void unknownGroup_throwsNotFound() {
      UUID studentId = UUID.randomUUID();
      UUID groupId = UUID.randomUUID();
      when(userRepository.findById(studentId))
          .thenReturn(
              Optional.of(new JUser(studentId, "A", "M", Role.STUDENT, "s@m.h", "enc", null)));
      when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> groupService.assignStudent(studentId, groupId))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessage("Group not found: " + groupId);
    }

    @Test
    void validAssignment_savesGroupHistory() {
      UUID studentId = UUID.randomUUID();
      UUID groupId = UUID.randomUUID();
      JUser student = new JUser(studentId, "A", "M", Role.STUDENT, "s@m.h", "enc", null);
      JGroup group = new JGroup(groupId, "G1");
      when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
      when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));

      JGroupHistory saved =
          new JGroupHistory(UUID.randomUUID(), group, student, LocalDateTime.now(), null);
      when(groupHistoryRepository.save(any(JGroupHistory.class))).thenReturn(saved);
      GroupHistory expected = GroupHistory.builder().id(saved.getId()).build();
      when(groupHistoryMapper.toDomain(saved)).thenReturn(expected);

      GroupHistory result = groupService.assignStudent(studentId, groupId);

      assertThat(result.id()).isEqualTo(saved.getId());
      verify(groupHistoryRepository).save(any(JGroupHistory.class));
    }
  }

  @Nested
  class MoveStudent {

    @Test
    void closesOpenHistoriesAndAssignsNewGroup() {
      UUID studentId = UUID.randomUUID();
      UUID newGroupId = UUID.randomUUID();
      JGroupHistory openHistory =
          new JGroupHistory(
              UUID.randomUUID(),
              new JGroup(UUID.randomUUID(), "old"),
              null,
              LocalDateTime.now(),
              null);
      when(groupHistoryRepository.findByStudent_IdAndEndDateIsNull(studentId))
          .thenReturn(List.of(openHistory));

      JUser student = new JUser(studentId, "A", "M", Role.STUDENT, "s@m.h", "enc", null);
      JGroup newGroup = new JGroup(newGroupId, "new");
      when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
      when(groupRepository.findById(newGroupId)).thenReturn(Optional.of(newGroup));
      JGroupHistory saved =
          new JGroupHistory(UUID.randomUUID(), newGroup, student, LocalDateTime.now(), null);
      when(groupHistoryRepository.save(any(JGroupHistory.class))).thenReturn(saved);
      GroupHistory expected = GroupHistory.builder().id(saved.getId()).build();
      when(groupHistoryMapper.toDomain(saved)).thenReturn(expected);

      GroupHistory result = groupService.moveStudent(studentId, newGroupId);

      assertThat(openHistory.getEndDate()).isNotNull();
      ArgumentCaptor<List<JGroupHistory>> captor = ArgumentCaptor.forClass(List.class);
      verify(groupHistoryRepository).saveAll(captor.capture());
      assertThat(captor.getValue()).hasSize(1);
      assertThat(result).isEqualTo(expected);
    }
  }

  @Nested
  class GetStudentsInGroup {

    @Test
    void returnsStudentsInGroup() {
      UUID groupId = UUID.randomUUID();
      UUID studentId = UUID.randomUUID();
      JUser student = new JUser(studentId, "A", "M", Role.STUDENT, "s@m.h", "enc", null);
      JGroupHistory history =
          new JGroupHistory(UUID.randomUUID(), null, student, LocalDateTime.now(), null);
      when(groupHistoryRepository.findByGroup_IdAndEndDateIsNull(groupId))
          .thenReturn(List.of(history));
      User expected = User.builder().id(studentId).build();
      when(userMapper.toDomain(student)).thenReturn(expected);

      List<User> students = groupService.getStudentsInGroup(groupId);

      assertThat(students).containsExactly(expected);
    }

    @Test
    void returnsEmptyListWhenNoStudents() {
      UUID groupId = UUID.randomUUID();
      when(groupHistoryRepository.findByGroup_IdAndEndDateIsNull(groupId)).thenReturn(List.of());

      assertThat(groupService.getStudentsInGroup(groupId)).isEmpty();
    }
  }
}
