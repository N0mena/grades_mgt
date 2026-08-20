package hei.school.minou.endpoint.rest.controller.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.school.minou.endpoint.rest.controller.dto.ExamRequest;
import hei.school.minou.endpoint.rest.controller.dto.GroupIdsRequest;
import hei.school.minou.entity.Exam;
import hei.school.minou.service.ExamService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ExamControllerIT {

  @Mock private ExamService examService;

  private ExamController examController;

  @BeforeEach
  void setUp() {
    examController = new ExamController(examService);
  }

  @Test
  void should_return_all_exams() {
    Exam exam = mock(Exam.class);
    when(examService.getAllExams()).thenReturn(List.of(exam));

    List<Exam> result = examController.getExams();

    assertThat(result).containsExactly(exam);
    verify(examService).getAllExams();
  }

  @Test
  void should_return_exam_by_id() {
    UUID id = UUID.randomUUID();
    Exam exam = mock(Exam.class);
    when(examService.getExamById(id)).thenReturn(exam);

    Exam result = examController.getExamById(id);

    assertThat(result).isEqualTo(exam);
    verify(examService).getExamById(id);
  }

  @Test
  void should_return_exams_by_course() {
    UUID courseId = UUID.randomUUID();
    Exam exam = mock(Exam.class);
    when(examService.getExamsByCourse(courseId)).thenReturn(List.of(exam));

    List<Exam> result = examController.getExamsByCourse(courseId);

    assertThat(result).containsExactly(exam);
    verify(examService).getExamsByCourse(courseId);
  }

  @Test
  void should_create_exam() {
    UUID courseId = UUID.randomUUID();
    LocalDateTime date = LocalDateTime.now();
    ExamRequest request = new ExamRequest(courseId, date, 2.0f, List.of());
    Exam exam = mock(Exam.class);
    when(examService.createExam(courseId, date, 2.0f, List.of())).thenReturn(exam);

    Exam result = examController.createExam(request);

    assertThat(result).isEqualTo(exam);
    verify(examService).createExam(courseId, date, 2.0f, List.of());
  }

  @Test
  void should_link_groups_to_exam() {
    UUID examId = UUID.randomUUID();
    UUID groupId = UUID.randomUUID();
    GroupIdsRequest request = new GroupIdsRequest(List.of(groupId));
    Exam exam = mock(Exam.class);
    when(examService.getExamById(examId)).thenReturn(exam);

    Exam result = examController.linkGroups(examId, request);

    assertThat(result).isEqualTo(exam);
    verify(examService).linkGroups(examId, List.of(groupId));
  }
}
