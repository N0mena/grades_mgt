package hei.school.minou.endpoint.rest.controller.controller;

import hei.school.minou.endpoint.rest.controller.dto.ExamRequest;
import hei.school.minou.endpoint.rest.controller.dto.GroupIdsRequest;
import hei.school.minou.entity.Exam;
import hei.school.minou.service.ExamService;
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
public class ExamController {

  private final ExamService examService;

  @GetMapping("/exams")
  public List<Exam> getExams() {
    return examService.getAllExams();
  }

  @GetMapping("/exams/{id}")
  public Exam getExamById(@PathVariable UUID id) {
    return examService.getExamById(id);
  }

  @GetMapping("/exams/course/{courseId}")
  public List<Exam> getExamsByCourse(@PathVariable UUID courseId) {
    return examService.getExamsByCourse(courseId);
  }

  @PostMapping("/exams")
  public Exam createExam(@RequestBody ExamRequest request) {
    return examService.createExam(
        request.courseId(), request.examDate(), request.coefficient(), request.groupIds());
  }

  @PostMapping("/exams/{examId}/groups")
  public Exam linkGroups(@PathVariable UUID examId, @RequestBody GroupIdsRequest request) {
    examService.linkGroups(examId, request.groupIds());
    return examService.getExamById(examId);
  }
}
