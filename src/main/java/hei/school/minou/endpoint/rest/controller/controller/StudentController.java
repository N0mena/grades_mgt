package hei.school.minou.endpoint.rest.controller.controller;

import hei.school.minou.endpoint.rest.controller.dto.TranscriptResponse;
import hei.school.minou.entity.Grade;
import hei.school.minou.entity.GradeHistory;
import hei.school.minou.entity.Group;
import hei.school.minou.entity.User;
import hei.school.minou.entity.enums.Role;
import hei.school.minou.exception.ForbiddenOperationException;
import hei.school.minou.security.SecurityUtils;
import hei.school.minou.service.GradeHistoryService;
import hei.school.minou.service.GradeService;
import hei.school.minou.service.GroupService;
import hei.school.minou.service.TranscriptService;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/students")
@AllArgsConstructor
public class StudentController {

  private final SecurityUtils securityUtils;
  private final GradeService gradeService;
  private final GradeHistoryService gradeHistoryService;
  private final GroupService groupService;
  private final TranscriptService transcriptService;

  @GetMapping("/me")
  public User getMyProfile() {
    return requireStudent(securityUtils.currentUser());
  }

  @GetMapping("/me/grades")
  public List<Grade> getMyGrades() {
    User current = requireStudent(securityUtils.currentUser());
    return gradeService.getGradesByStudent(current.id(), current);
  }

  @GetMapping("/me/grades/{gradeId}/history")
  public List<GradeHistory> getMyGradeHistory(@PathVariable UUID gradeId) {
    User current = requireStudent(securityUtils.currentUser());
    return gradeHistoryService.getHistoryByGrade(gradeId, current);
  }

  @GetMapping("/me/transcript")
  public TranscriptResponse getMyTranscript() {
    User current = requireStudent(securityUtils.currentUser());
    return transcriptService.getTranscript(current.id(), current);
  }

  @PostMapping("/me/transcript/send")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void sendMyTranscript() {
    User current = requireStudent(securityUtils.currentUser());
    transcriptService.requestTranscriptSend(current.id(), current);
  }

  @GetMapping("/me/group")
  public Group getMyGroup() {
    User current = requireStudent(securityUtils.currentUser());
    return groupService.getCurrentGroup(current.id());
  }

  @GetMapping("/{studentId}/grades")
  public List<Grade> getStudentGrades(@PathVariable UUID studentId) {
    User current = securityUtils.currentUser();
    securityUtils.assertAdmin(current);
    return gradeService.getGradesByStudent(studentId, current);
  }

  @GetMapping("/{studentId}/transcript/send")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void sendStudentTranscript(@PathVariable UUID studentId) {
    User current = securityUtils.currentUser();
    securityUtils.assertAdmin(current);
    transcriptService.requestTranscriptSend(studentId, current);
  }

  @PostMapping("/{studentId}/transcript")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void requestTranscript(@PathVariable UUID studentId) {
    transcriptService.requestTranscriptSend(studentId, securityUtils.currentUser());
  }

  private User requireStudent(User user) {
    if (user.role() != Role.STUDENT) {
      throw new ForbiddenOperationException("Only a student can access /students/me endpoints");
    }
    return user;
  }
}
