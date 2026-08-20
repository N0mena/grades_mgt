package hei.school.minou.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.school.minou.entity.GroupHistory;
import hei.school.minou.entity.User;
import hei.school.minou.entity.enums.Role;
import hei.school.minou.exception.ForbiddenOperationException;
import hei.school.minou.mapper.GroupHistoryMapper;
import hei.school.minou.repository.GroupHistoryRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class GroupHistoryServiceTest {

  private GroupHistoryRepository groupHistoryRepository;
  private GroupHistoryMapper groupHistoryMapper;
  private GroupHistoryService groupHistoryService;

  @BeforeEach
  void setUp() {
    groupHistoryRepository = mock(GroupHistoryRepository.class);
    groupHistoryMapper = mock(GroupHistoryMapper.class);
    groupHistoryService = new GroupHistoryService(groupHistoryRepository, groupHistoryMapper);
  }

  @Nested
  class GetHistoryByStudent {

    @Test
    void student_viewingAnotherStudent_throwsForbidden() {
      User viewer = user(Role.STUDENT);
      UUID otherStudentId = UUID.randomUUID();

      assertThatThrownBy(() -> groupHistoryService.getHistoryByStudent(otherStudentId, viewer))
          .isInstanceOf(ForbiddenOperationException.class)
          .hasMessage("A student can only view their own group history");
    }

    @Test
    void student_viewingSelf_ok() {
      User viewer = user(Role.STUDENT);
      viewer = User.builder().id(viewer.id()).role(Role.STUDENT).build();
      when(groupHistoryRepository.findByStudent_Id(viewer.id())).thenReturn(List.of());
      when(groupHistoryMapper.toDomain(null)).thenReturn(null);

      List<GroupHistory> result = groupHistoryService.getHistoryByStudent(viewer.id(), viewer);

      assertThat(result).isEmpty();
      verify(groupHistoryRepository).findByStudent_Id(viewer.id());
    }

    @Test
    void teacher_throwsForbidden() {
      User viewer = user(Role.TEACHER);

      assertThatThrownBy(() -> groupHistoryService.getHistoryByStudent(UUID.randomUUID(), viewer))
          .isInstanceOf(ForbiddenOperationException.class)
          .hasMessage("A teacher cannot access group history");
    }

    @Test
    void admin_canViewAnyStudent() {
      User viewer = user(Role.ADMIN);
      UUID studentId = UUID.randomUUID();
      when(groupHistoryRepository.findByStudent_Id(studentId)).thenReturn(List.of());

      List<GroupHistory> result = groupHistoryService.getHistoryByStudent(studentId, viewer);

      assertThat(result).isEmpty();
      verify(groupHistoryRepository).findByStudent_Id(studentId);
    }
  }

  private static User user(Role role) {
    return User.builder().id(UUID.randomUUID()).role(role).email("u@h.s").build();
  }
}
