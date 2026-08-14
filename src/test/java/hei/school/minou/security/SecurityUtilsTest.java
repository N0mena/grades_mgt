package hei.school.minou.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.school.minou.entity.User;
import hei.school.minou.entity.enums.Role;
import hei.school.minou.exception.ForbiddenOperationException;
import hei.school.minou.mapper.UserMapper;
import hei.school.minou.repository.CourseAssignementRepository;
import hei.school.minou.repository.UserRepository;
import hei.school.minou.repository.model.JCourse;
import hei.school.minou.repository.model.JGrade;
import hei.school.minou.repository.model.JUser;
import hei.school.minou.service.auth.AuthPrincipal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class SecurityUtilsTest {

  private CourseAssignementRepository courseAssignementRepository;
  private UserRepository userRepository;
  private UserMapper userMapper;
  private SecurityUtils securityUtils;

  @BeforeEach
  void setUp() {
    courseAssignementRepository = mock(CourseAssignementRepository.class);
    userRepository = mock(UserRepository.class);
    userMapper = mock(UserMapper.class);
    securityUtils = new SecurityUtils(courseAssignementRepository, userRepository, userMapper);
  }

  @Test
  void teacherTeachesCourse_delegatesToRepository() {
    UUID teacherId = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();
    when(courseAssignementRepository.existsByCourse_IdAndTeacher_Id(courseId, teacherId))
        .thenReturn(true);

    assertThat(securityUtils.teacherTeachesCourse(teacherId, courseId)).isTrue();
    verify(courseAssignementRepository).existsByCourse_IdAndTeacher_Id(courseId, teacherId);
  }

  @Nested
  class AssertCanViewStudentGrades {

    @Test
    void student_viewingHimself_ok() {
      User student = student(UUID.randomUUID());
      assertThatCode(() -> securityUtils.assertCanViewStudentGrades(student, student.id()))
          .doesNotThrowAnyException();
      verify(courseAssignementRepository, never()).existsByCourse_IdAndTeacher_Id(null, null);
    }

    @Test
    void student_viewingAnotherStudent_throwsForbidden() {
      User student = student(UUID.randomUUID());
      UUID otherId = UUID.randomUUID();
      assertThatThrownBy(() -> securityUtils.assertCanViewStudentGrades(student, otherId))
          .isInstanceOf(ForbiddenOperationException.class)
          .hasMessage("A student can only view their own grades");
    }

    @Test
    void teacher_orAdmin_canViewAnyStudent() {
      User teacher = user(Role.TEACHER, UUID.randomUUID());
      User admin = user(Role.ADMIN, UUID.randomUUID());
      UUID anyStudentId = UUID.randomUUID();

      assertThatCode(() -> securityUtils.assertCanViewStudentGrades(teacher, anyStudentId))
          .doesNotThrowAnyException();
      assertThatCode(() -> securityUtils.assertCanViewStudentGrades(admin, anyStudentId))
          .doesNotThrowAnyException();
    }
  }

  @Nested
  class AssertCanViewGrade {

    @Test
    void admin_canViewAnything() {
      JGrade grade = newGrade();
      assertThatCode(
              () -> securityUtils.assertCanViewGrade(user(Role.ADMIN, UUID.randomUUID()), grade))
          .doesNotThrowAnyException();
    }

    @Test
    void student_ownsGrade_ok() {
      UUID studentId = UUID.randomUUID();
      JGrade grade = newGrade(studentId);
      assertThatCode(() -> securityUtils.assertCanViewGrade(student(studentId), grade))
          .doesNotThrowAnyException();
    }

    @Test
    void student_onAnotherGrade_throwsForbidden() {
      JGrade grade = newGrade(UUID.randomUUID());
      assertThatThrownBy(() -> securityUtils.assertCanViewGrade(student(UUID.randomUUID()), grade))
          .isInstanceOf(ForbiddenOperationException.class)
          .hasMessage("A student can only view their own grades");
    }

    @Test
    void teacher_teachesCourse_ok() {
      UUID teacherId = UUID.randomUUID();
      UUID courseId = UUID.randomUUID();
      when(courseAssignementRepository.existsByCourse_IdAndTeacher_Id(courseId, teacherId))
          .thenReturn(true);

      assertThatCode(
              () ->
                  securityUtils.assertCanViewGrade(
                      user(Role.TEACHER, teacherId), newGradeWithCourse(courseId)))
          .doesNotThrowAnyException();

      verify(courseAssignementRepository).existsByCourse_IdAndTeacher_Id(courseId, teacherId);
    }

    @Test
    void teacher_notTeachingCourse_throwsForbidden() {
      UUID teacherId = UUID.randomUUID();
      UUID courseId = UUID.randomUUID();

      assertThatThrownBy(
              () ->
                  securityUtils.assertCanViewGrade(
                      user(Role.TEACHER, teacherId), newGradeWithCourse(courseId)))
          .isInstanceOf(ForbiddenOperationException.class)
          .hasMessage("A teacher can only view grades of their courses");
    }
  }

  @Nested
  class AssertCanModifyGrade {

    @Test
    void admin_canModify() {
      JGrade grade = newGrade();
      assertThatCode(
              () -> securityUtils.assertCanModifyGrade(user(Role.ADMIN, UUID.randomUUID()), grade))
          .doesNotThrowAnyException();
    }

    @Test
    void teacher_teachesCourse_canModify() {
      UUID teacherId = UUID.randomUUID();
      UUID courseId = UUID.randomUUID();
      when(courseAssignementRepository.existsByCourse_IdAndTeacher_Id(courseId, teacherId))
          .thenReturn(true);

      assertThatCode(
              () ->
                  securityUtils.assertCanModifyGrade(
                      user(Role.TEACHER, teacherId), newGradeWithCourse(courseId)))
          .doesNotThrowAnyException();
    }

    @Test
    void teacher_notTeaching_throwsForbidden_whenRequiringReason() {
      UUID teacherId = UUID.randomUUID();
      UUID courseId = UUID.randomUUID();
      assertThatThrownBy(
              () ->
                  securityUtils.assertCanModifyGrade(
                      user(Role.TEACHER, teacherId), newGradeWithCourse(courseId)))
          .isInstanceOf(ForbiddenOperationException.class)
          .hasMessage("Only the teacher of the course can modify this grade");
    }

    @Test
    void student_cannotModify() {
      JGrade grade = newGrade();
      assertThatThrownBy(
              () -> securityUtils.assertCanModifyGrade(student(UUID.randomUUID()), grade))
          .isInstanceOf(ForbiddenOperationException.class)
          .hasMessage("Only a teacher or an admin can modify grades");
    }
  }

  @Nested
  class CurrentUser {

    @Test
    void resolvesAuthenticatedPrincipalToUser() {
      UUID userId = UUID.randomUUID();
      JUser jUser = jUser(userId, "alida@hei.school");
      User expected = User.builder().id(userId).email("alida@hei.school").build();
      when(userRepository.findById(userId)).thenReturn(Optional.of(jUser));
      when(userMapper.toDomain(jUser)).thenReturn(expected);

      SecurityContextHolder.getContext()
          .setAuthentication(
              new UsernamePasswordAuthenticationToken(
                  new AuthPrincipal(userId, Role.STUDENT, "alida@hei.school"), null));

      assertThat(securityUtils.currentUser()).isEqualTo(expected);
    }

    @Test
    void withoutAuthentication_throwsForbidden() {
      SecurityContextHolder.clearContext();
      assertThatThrownBy(() -> securityUtils.currentUser())
          .isInstanceOf(ForbiddenOperationException.class)
          .hasMessage("Not authenticated");
    }
  }

  private static User student(UUID id) {
    return user(Role.STUDENT, id);
  }

  private static User user(Role role, UUID id) {
    return User.builder().id(id).role(role).email("user@hei.school").build();
  }

  private static JUser jUser(UUID id, String email) {
    return new JUser(id, "Alida", "Minou", Role.STUDENT, email, "encrypted", null);
  }

  private static JGrade newGrade() {
    return newGradeWithCourse(UUID.randomUUID());
  }

  private static JGrade newGrade(UUID studentId) {
    JGrade grade = newGrade();
    grade.setStudent(jUser(studentId, "student@hei.school"));
    return grade;
  }

  private static JGrade newGradeWithCourse(UUID courseId) {
    JCourse course = new JCourse(courseId, "PROG4", "Programming", 4);
    JGrade grade = new JGrade();
    grade.setCourse(course);
    grade.setStudent(jUser(UUID.randomUUID(), "student@hei.school"));
    return grade;
  }
}
