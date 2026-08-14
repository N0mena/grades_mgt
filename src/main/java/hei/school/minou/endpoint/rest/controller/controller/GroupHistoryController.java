package hei.school.minou.endpoint.rest.controller.controller;

import hei.school.minou.endpoint.rest.controller.dto.ChangeGroupRequest;
import hei.school.minou.entity.GroupHistory;
import hei.school.minou.entity.User;
import hei.school.minou.security.SecurityUtils;
import hei.school.minou.service.GroupHistoryService;
import hei.school.minou.service.GroupService;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class GroupHistoryController {

  private final GroupHistoryService groupHistoryService;
  private final GroupService groupService;
  private final SecurityUtils securityUtils;

  @GetMapping("/students/{studentId}/group-history")
  public List<GroupHistory> getGroupHistory(@PathVariable UUID studentId) {
    return groupHistoryService.getHistoryByStudent(studentId, securityUtils.currentUser());
  }

  @PutMapping("/students/{studentId}/group")
  public GroupHistory changeGroup(
      @PathVariable UUID studentId, @RequestBody ChangeGroupRequest request) {
    User actor = securityUtils.currentUser();
    return groupService.moveStudent(studentId, request.newGroupId());
  }
}
