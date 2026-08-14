package hei.school.minou.endpoint.rest.controller.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.school.minou.entity.Group;
import hei.school.minou.entity.User;
import hei.school.minou.service.GroupService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GroupControllerIT {
  @Mock private GroupService groupService;

  private GroupController groupController;

  @BeforeEach
  void setUp() {
    groupController = new GroupController(groupService);
  }

  @Test
  void should_return_all_groups() {
    Group group = mock(Group.class);
    when(groupService.getAllGroups()).thenReturn(List.of(group));

    List<Group> result = groupController.getGroups();

    assertThat(result).containsExactly(group);
    verify(groupService).getAllGroups();
  }

  @Test
  void should_return_group_by_id() {
    UUID groupId = UUID.randomUUID();
    Group group = mock(Group.class);
    when(groupService.getGroupById(groupId)).thenReturn(group);

    Group result = groupController.getGroupById(groupId);

    assertThat(result).isEqualTo(group);
    verify(groupService).getGroupById(groupId);
  }

  @Test
  void should_create_group() {
    Group groupToCreate = mock(Group.class);
    Group savedGroup = mock(Group.class);
    when(groupService.saveGroup(groupToCreate)).thenReturn(savedGroup);

    Group result = groupController.createGroup(groupToCreate);

    assertThat(result).isEqualTo(savedGroup);
    verify(groupService).saveGroup(groupToCreate);
  }

  @Test
  void should_return_students_in_group() {
    UUID groupId = UUID.randomUUID();
    User student = mock(User.class);
    when(groupService.getStudentsInGroup(groupId)).thenReturn(List.of(student));

    List<User> result = groupController.getStudentsInGroup(groupId);

    assertThat(result).containsExactly(student);
    verify(groupService).getStudentsInGroup(groupId);
  }

  @Test
  void should_return_empty_list_when_group_has_no_students() {
    UUID groupId = UUID.randomUUID();
    when(groupService.getStudentsInGroup(groupId)).thenReturn(List.of());

    List<User> result = groupController.getStudentsInGroup(groupId);

    assertThat(result).isEmpty();
    verify(groupService).getStudentsInGroup(groupId);
  }
}
