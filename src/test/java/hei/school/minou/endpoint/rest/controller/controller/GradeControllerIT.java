package hei.school.minou.endpoint.rest.controller.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.school.minou.endpoint.rest.controller.dto.GradeRequest;
import hei.school.minou.endpoint.rest.controller.dto.UpdateGradeRequest;
import hei.school.minou.entity.Grade;
import hei.school.minou.entity.GradeHistory;
import hei.school.minou.entity.User;
import hei.school.minou.security.SecurityUtils;
import hei.school.minou.service.GradeHistoryService;
import hei.school.minou.service.GradeService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GradeControllerIT {
  @Mock private GradeService gradeService;
  @Mock private GradeHistoryService gradeHistoryService;
  @Mock private SecurityUtils securityUtils;

  private GradeController gradeController;

  private User currentUser;

  @BeforeEach
  void setUp() {
    gradeController = new GradeController(gradeService, gradeHistoryService, securityUtils);
    currentUser = mock(User.class);
  }

  @Test
  void should_return_all_grades_for_current_user() {
    when(securityUtils.currentUser()).thenReturn(currentUser);
    Grade grade = mock(Grade.class);
    when(gradeService.getAllGrades(currentUser)).thenReturn(List.of(grade));

    List<Grade> result = gradeController.getAllGrades();

    assertThat(result).containsExactly(grade);
    verify(gradeService).getAllGrades(currentUser);
  }

  @Test
  void should_return_grades_by_student() {
    UUID studentId = UUID.randomUUID();
    when(securityUtils.currentUser()).thenReturn(currentUser);
    Grade grade = mock(Grade.class);
    when(gradeService.getGradesByStudent(studentId, currentUser)).thenReturn(List.of(grade));

    List<Grade> result = gradeController.getGradesByStudent(studentId);

    assertThat(result).containsExactly(grade);
    verify(gradeService).getGradesByStudent(studentId, currentUser);
  }

  @Test
  void should_return_grades_by_course() {
    UUID courseId = UUID.randomUUID();
    when(securityUtils.currentUser()).thenReturn(currentUser);
    Grade grade = mock(Grade.class);
    when(gradeService.getGradesByCourse(courseId, currentUser)).thenReturn(List.of(grade));

    List<Grade> result = gradeController.getGradesByCourse(courseId);

    assertThat(result).containsExactly(grade);
    verify(gradeService).getGradesByCourse(courseId, currentUser);
  }

  @Test
  void should_return_grade_by_id() {
    UUID gradeId = UUID.randomUUID();
    when(securityUtils.currentUser()).thenReturn(currentUser);
    Grade grade = mock(Grade.class);
    when(gradeService.getGradeById(gradeId, currentUser)).thenReturn(grade);

    Grade result = gradeController.getGradeById(gradeId);

    assertThat(result).isEqualTo(grade);
    verify(gradeService).getGradeById(gradeId, currentUser);
  }

  @Test
  void should_return_grade_history() {
    UUID gradeId = UUID.randomUUID();
    when(securityUtils.currentUser()).thenReturn(currentUser);
    GradeHistory history = mock(GradeHistory.class);
    when(gradeHistoryService.getHistoryByGrade(gradeId, currentUser)).thenReturn(List.of(history));

    List<GradeHistory> result = gradeController.getGradeHistory(gradeId);

    assertThat(result).containsExactly(history);
    verify(gradeHistoryService).getHistoryByGrade(gradeId, currentUser);
  }

  @Test
  void should_create_grade() {
    UUID studentId = UUID.randomUUID();
    UUID teacherId = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();
    UUID examId = UUID.randomUUID();
    double value = 15.5;
    GradeRequest request = new GradeRequest(studentId, teacherId, courseId, examId, (float) value);
    Grade createdGrade = mock(Grade.class);

    when(securityUtils.currentUser()).thenReturn(currentUser);
    when(gradeService.createGrade(
            studentId, teacherId, courseId, examId, (float) value, currentUser))
        .thenReturn(createdGrade);

    Grade result = gradeController.createGrade(request);

    assertThat(result).isEqualTo(createdGrade);
    verify(gradeService)
        .createGrade(studentId, teacherId, courseId, examId, (float) value, currentUser);
  }

  @Test
  void should_update_grade() {
    UUID gradeId = UUID.randomUUID();
    double newValue = 18.0;
    String reason = "Erreur de saisie corrigée";
    UpdateGradeRequest request = new UpdateGradeRequest((float) newValue, reason);
    Grade updatedGrade = mock(Grade.class);

    when(securityUtils.currentUser()).thenReturn(currentUser);
    when(gradeService.updateGrade(gradeId, (float) newValue, reason, currentUser))
        .thenReturn(updatedGrade);

    Grade result = gradeController.updateGrade(gradeId, request);

    assertThat(result).isEqualTo(updatedGrade);
    verify(gradeService).updateGrade(gradeId, (float) newValue, reason, currentUser);
  }
}
