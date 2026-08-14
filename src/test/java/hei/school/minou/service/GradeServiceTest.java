package hei.school.minou.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.school.minou.entity.Grade;
import hei.school.minou.entity.User;
import hei.school.minou.entity.enums.Role;
import hei.school.minou.exception.BadRequestException;
import hei.school.minou.exception.ForbiddenOperationException;
import hei.school.minou.exception.ResourceNotFoundException;
import hei.school.minou.mapper.GradeHistoryMapper;
import hei.school.minou.mapper.GradeMapper;
import hei.school.minou.repository.CourseAssignementRepository;
import hei.school.minou.repository.CourseRepository;
import hei.school.minou.repository.ExamRepository;
import hei.school.minou.repository.GradeHistoryRepository;
import hei.school.minou.repository.GradeRepository;
import hei.school.minou.repository.UserRepository;
import hei.school.minou.repository.model.JCourse;
import hei.school.minou.repository.model.JGrade;
import hei.school.minou.repository.model.JGradeHistory;
import hei.school.minou.repository.model.JUser;
import hei.school.minou.security.SecurityUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class GradeServiceTest {

  private GradeRepository gradeRepository;
  private GradeHistoryRepository gradeHistoryRepository;
  private UserRepository userRepository;
  private CourseRepository courseRepository;
  private ExamRepository examRepository;
  private CourseAssignementRepository courseAssignementRepository;
  private SecurityUtils securityUtils;
  private GradeMapper gradeMapper;
  private GradeHistoryMapper gradeHistoryMapper;
  private GradeService gradeService;

  @BeforeEach
  void setUp() {
    gradeRepository = mock(GradeRepository.class);
    gradeHistoryRepository = mock(GradeHistoryRepository.class);
    userRepository = mock(UserRepository.class);
    courseRepository = mock(CourseRepository.class);
    examRepository = mock(ExamRepository.class);
    courseAssignementRepository = mock(CourseAssignementRepository.class);
    securityUtils = mock(SecurityUtils.class);
    gradeMapper = mock(GradeMapper.class);
    gradeHistoryMapper = mock(GradeHistoryMapper.class);
    gradeService =
        new GradeService(
            gradeRepository,
            gradeHistoryRepository,
            userRepository,
            courseRepository,
            examRepository,
            courseAssignementRepository,
            securityUtils,
            gradeMapper,
            gradeHistoryMapper);
  }

  @Nested
  class GetAllGrades {

    @Test
    void admin_getsAllGrades() {
      User admin = user(Role.ADMIN);
      JGrade jGrade = newJGrade();
      when(gradeRepository.findAll()).thenReturn(List.of(jGrade));
      when(gradeMapper.toDomain(jGrade)).thenReturn(Grade.builder().id(jGrade.getId()).build());

      List<Grade> grades = gradeService.getAllGrades(admin);

      assertThat(grades).hasSize(1);
      verify(gradeRepository).findAll();
    }

    @Test
    void nonAdmin_throwsForbidden() {
      assertThatThrownBy(() -> gradeService.getAllGrades(user(Role.STUDENT)))
          .isInstanceOf(ForbiddenOperationException.class)
          .hasMessage("Only an admin can view all grades");
      verify(gradeRepository, never()).findAll();
    }
  }

  @Nested
  class GetGradesByCourse {

    @Test
    void student_throwsForbidden() {
      assertThatThrownBy(
              () -> gradeService.getGradesByCourse(UUID.randomUUID(), user(Role.STUDENT)))
          .isInstanceOf(ForbiddenOperationException.class)
          .hasMessage("A student cannot view all grades of a course");
    }

    @Test
    void teacher_notTeachingCourse_throwsForbidden() {
      User teacher = user(Role.TEACHER);
      UUID courseId = UUID.randomUUID();
      when(securityUtils.teacherTeachesCourse(teacher.id(), courseId)).thenReturn(false);

      assertThatThrownBy(() -> gradeService.getGradesByCourse(courseId, teacher))
          .isInstanceOf(ForbiddenOperationException.class)
          .hasMessage("A teacher can only view grades of their courses");
    }

    @Test
    void teacher_teachingCourse_getsGrades() {
      User teacher = user(Role.TEACHER);
      UUID courseId = UUID.randomUUID();
      when(securityUtils.teacherTeachesCourse(teacher.id(), courseId)).thenReturn(true);
      JGrade jGrade = newJGrade();
      when(gradeRepository.findByCourse_Id(courseId)).thenReturn(List.of(jGrade));
      when(gradeMapper.toDomain(jGrade)).thenReturn(Grade.builder().id(jGrade.getId()).build());

      List<Grade> grades = gradeService.getGradesByCourse(courseId, teacher);

      assertThat(grades).hasSize(1);
    }
  }

  @Nested
  class GetGradeById {

    @Test
    void unknownGrade_throwsNotFound() {
      UUID gradeId = UUID.randomUUID();
      when(gradeRepository.findById(gradeId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> gradeService.getGradeById(gradeId, user(Role.ADMIN)))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessage("Grade not found: " + gradeId);
    }

    @Test
    void knownGrade_isCheckedThenReturned() {
      JGrade jGrade = newJGrade();
      Grade expected = Grade.builder().id(jGrade.getId()).build();
      when(gradeRepository.findById(jGrade.getId())).thenReturn(Optional.of(jGrade));
      when(gradeMapper.toDomain(jGrade)).thenReturn(expected);

      Grade result = gradeService.getGradeById(jGrade.getId(), user(Role.ADMIN));

      assertThat(result).isEqualTo(expected);
      verify(securityUtils).assertCanViewGrade(any(User.class), any(JGrade.class));
    }
  }

  @Nested
  class CreateGrade {

    @Test
    void unknownCourse_throwsNotFound() {
      UUID courseId = UUID.randomUUID();
      when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

      assertThatThrownBy(
              () ->
                  gradeService.createGrade(
                      UUID.randomUUID(),
                      UUID.randomUUID(),
                      courseId,
                      null,
                      12.0f,
                      user(Role.ADMIN)))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessage("Course not found: " + courseId);
    }

    @Test
    void teacherNotTeachingCourse_cannotGrade() {
      UUID courseId = UUID.randomUUID();
      User teacher = user(Role.TEACHER);
      when(courseRepository.findById(courseId))
          .thenReturn(Optional.of(new JCourse(courseId, "P", "T", 1)));
      when(securityUtils.teacherTeachesCourse(teacher.id(), courseId)).thenReturn(false);

      assertThatThrownBy(
              () ->
                  gradeService.createGrade(
                      UUID.randomUUID(), teacher.id(), courseId, null, 12.0f, teacher))
          .isInstanceOf(ForbiddenOperationException.class)
          .hasMessage("Only the teacher of the course or an admin can grade it");
    }

    @Test
    void admin_canGradeAnyCourse() {
      UUID courseId = UUID.randomUUID();
      User admin = user(Role.ADMIN);
      UUID studentId = UUID.randomUUID();
      UUID teacherId = UUID.randomUUID();
      when(courseRepository.findById(courseId))
          .thenReturn(Optional.of(new JCourse(courseId, "P", "T", 1)));
      when(userRepository.findById(studentId)).thenReturn(Optional.of(jUser(studentId, "s@h.s")));
      when(userRepository.findById(teacherId)).thenReturn(Optional.of(jUser(teacherId, "t@h.s")));
      JGrade saved = newJGrade();
      when(gradeRepository.save(any(JGrade.class))).thenReturn(saved);
      when(gradeMapper.toDomain(saved)).thenReturn(Grade.builder().id(saved.getId()).build());

      Grade grade = gradeService.createGrade(studentId, teacherId, courseId, null, 12.0f, admin);

      assertThat(grade.id()).isEqualTo(saved.getId());
      verify(gradeRepository).save(any(JGrade.class));
    }
  }

  @Nested
  class UpdateGrade {

    @Test
    void blankReason_throwsBadRequest() {
      assertThatThrownBy(
              () -> gradeService.updateGrade(UUID.randomUUID(), 15.0f, "  ", user(Role.TEACHER)))
          .isInstanceOf(BadRequestException.class)
          .hasMessage("A reason is required to modify a grade");
      verify(gradeRepository, never()).findById(any());
    }

    @Test
    void unknownGrade_throwsNotFound() {
      UUID gradeId = UUID.randomUUID();
      when(gradeRepository.findById(gradeId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> gradeService.updateGrade(gradeId, 15.0f, "typo", user(Role.ADMIN)))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessage("Grade not found: " + gradeId);
    }

    @Test
    void updatesValueAndSavesHistory() {
      User actor = user(Role.TEACHER);
      JGrade jGrade = newJGrade();
      jGrade.setValue(10.0f);
      JUser actorJpa = jUser(actor.id(), "actor@h.s");
      when(gradeRepository.findById(jGrade.getId())).thenReturn(Optional.of(jGrade));
      when(gradeRepository.save(jGrade)).thenReturn(jGrade);
      when(userRepository.findById(actor.id())).thenReturn(Optional.of(actorJpa));
      when(gradeMapper.toDomain(jGrade)).thenReturn(Grade.builder().id(jGrade.getId()).build());

      gradeService.updateGrade(jGrade.getId(), 15.0f, "erreur de saisie", actor);

      assertThat(jGrade.getValue()).isEqualTo(15.0f);
      verify(securityUtils).assertCanModifyGrade(actor, jGrade);

      ArgumentCaptor<JGradeHistory> historyCaptor = ArgumentCaptor.forClass(JGradeHistory.class);
      verify(gradeHistoryRepository).save(historyCaptor.capture());
      JGradeHistory history = historyCaptor.getValue();
      assertThat(history.getOldValue()).isEqualTo(10.0f);
      assertThat(history.getNewValue()).isEqualTo(15.0f);
      assertThat(history.getReason()).isEqualTo("erreur de saisie");
      assertThat(history.getModifiedBy()).isEqualTo(actorJpa);
      assertThat(history.getModifiedAt()).isNotNull();
    }
  }

  private static User user(Role role) {
    return User.builder().id(UUID.randomUUID()).role(role).email("u@h.s").build();
  }

  private static JUser jUser(UUID id, String email) {
    return new JUser(id, "A", "M", Role.STUDENT, email, "enc");
  }

  private static JGrade newJGrade() {
    JGrade grade = new JGrade();
    grade.setId(UUID.randomUUID());
    grade.setCreatedAt(LocalDateTime.now());
    grade.setCourse(new JCourse(UUID.randomUUID(), "P", "T", 1));
    return grade;
  }
}
