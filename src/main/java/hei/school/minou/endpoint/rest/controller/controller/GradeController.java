package hei.school.minou.endpoint.rest.controller.controller;

import hei.school.minou.endpoint.rest.controller.dto.GradeRequest;
import hei.school.minou.endpoint.rest.controller.dto.UpdateGradeRequest;
import hei.school.minou.entity.Grade;
import hei.school.minou.entity.GradeHistory;
import hei.school.minou.security.SecurityUtils;
import hei.school.minou.service.GradeHistoryService;
import hei.school.minou.service.GradeService;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class GradeController {

  private final GradeService gradeService;
  private final GradeHistoryService gradeHistoryService;
  private final SecurityUtils securityUtils;

  @GetMapping("/grades")
  public List<Grade> getAllGrades() {
    return gradeService.getAllGrades(securityUtils.currentUser());
  }

  @GetMapping("/grades/students/{studentId}")
  public List<Grade> getGradesByStudent(@PathVariable UUID studentId) {
    return gradeService.getGradesByStudent(studentId, securityUtils.currentUser());
  }

  @GetMapping("/grades/courses/{courseId}")
  public List<Grade> getGradesByCourse(@PathVariable UUID courseId) {
    return gradeService.getGradesByCourse(courseId, securityUtils.currentUser());
  }

  @GetMapping("/grades/{gradeId}")
  public Grade getGradeById(@PathVariable UUID gradeId) {
    return gradeService.getGradeById(gradeId, securityUtils.currentUser());
  }

  @GetMapping("/grades/{gradeId}/history")
  public List<GradeHistory> getGradeHistory(@PathVariable UUID gradeId) {
    return gradeHistoryService.getHistoryByGrade(gradeId, securityUtils.currentUser());
  }

  @PostMapping("/grades")
  public Grade createGrade(@RequestBody GradeRequest request) {
    return gradeService.createGrade(
        request.studentId(),
        request.teacherId(),
        request.courseId(),
        request.examId(),
        request.value(),
        securityUtils.currentUser());
  }

  @PutMapping("/grades/{gradeId}")
  public Grade updateGrade(@PathVariable UUID gradeId, @RequestBody UpdateGradeRequest request) {
    return gradeService.updateGrade(
        gradeId, request.value(), request.reason(), securityUtils.currentUser());
  }
}
