package hei.school.minou.endpoint.rest.controller.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.school.minou.endpoint.rest.controller.dto.ChangeGroupRequest;
import hei.school.minou.entity.GroupHistory;
import hei.school.minou.entity.User;
import hei.school.minou.security.SecurityUtils;
import hei.school.minou.service.GroupHistoryService;
import hei.school.minou.service.GroupService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GroupHistoryControllerIT {

  @Mock private GroupHistoryService groupHistoryService;
  @Mock private GroupService groupService;
  @Mock private SecurityUtils securityUtils;

  private GroupHistoryController groupHistoryController;

  private User currentUser;

  @BeforeEach
  void setUp() {
    groupHistoryController =
        new GroupHistoryController(groupHistoryService, groupService, securityUtils);
    currentUser = mock(User.class);
  }

  @Test
  void should_return_group_history_for_student() {
    UUID studentId = UUID.randomUUID();
    GroupHistory history = mock(GroupHistory.class);
    when(securityUtils.currentUser()).thenReturn(currentUser);
    when(groupHistoryService.getHistoryByStudent(studentId, currentUser))
        .thenReturn(List.of(history));

    List<GroupHistory> result = groupHistoryController.getGroupHistory(studentId);

    assertThat(result).containsExactly(history);
    verify(groupHistoryService).getHistoryByStudent(studentId, currentUser);
  }

  @Test
  void should_return_empty_history_when_student_has_no_group_changes() {
    UUID studentId = UUID.randomUUID();
    when(securityUtils.currentUser()).thenReturn(currentUser);
    when(groupHistoryService.getHistoryByStudent(studentId, currentUser)).thenReturn(List.of());

    List<GroupHistory> result = groupHistoryController.getGroupHistory(studentId);

    assertThat(result).isEmpty();
    verify(groupHistoryService).getHistoryByStudent(studentId, currentUser);
  }

  @Test
  void should_change_student_group() {
    UUID studentId = UUID.randomUUID();
    UUID newGroupId = UUID.randomUUID();
    ChangeGroupRequest request = new ChangeGroupRequest(newGroupId);
    GroupHistory resultingHistory = mock(GroupHistory.class);

    when(securityUtils.currentUser()).thenReturn(currentUser);
    when(groupService.moveStudent(studentId, newGroupId)).thenReturn(resultingHistory);

    GroupHistory result = groupHistoryController.changeGroup(studentId, request);

    assertThat(result).isEqualTo(resultingHistory);
    verify(securityUtils).currentUser();
    verify(groupService).moveStudent(studentId, newGroupId);
  }
}
