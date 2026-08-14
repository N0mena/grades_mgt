package hei.school.minou.endpoint.rest.controller.controller;

import hei.school.minou.entity.Group;
import hei.school.minou.entity.User;
import hei.school.minou.service.GroupService;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class GroupController {

  private final GroupService groupService;

  @GetMapping("/groups")
  public List<Group> getGroups() {
    return groupService.getAllGroups();
  }

  @GetMapping("/groups/{id}")
  public Group getGroupById(@PathVariable UUID id) {
    return groupService.getGroupById(id);
  }

  @PostMapping("/groups")
  public Group createGroup(@RequestBody Group group) {
    return groupService.saveGroup(group);
  }

  @GetMapping("/groups/{id}/students")
  public List<User> getStudentsInGroup(@PathVariable UUID id) {
    return groupService.getStudentsInGroup(id);
  }
}
