package hei.school.minou.security;

import hei.school.minou.entity.User;
import hei.school.minou.entity.enums.Role;
import hei.school.minou.exception.ForbiddenOperationException;
import hei.school.minou.exception.ResourceNotFoundException;
import hei.school.minou.mapper.UserMapper;
import hei.school.minou.repository.CourseAssignementRepository;
import hei.school.minou.repository.UserRepository;
import hei.school.minou.repository.model.JGrade;
import hei.school.minou.service.auth.AuthPrincipal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityUtils {

  private final CourseAssignementRepository courseAssignementRepository;
  private final UserRepository userRepository;
  private final UserMapper userMapper;

  public User currentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || !(authentication.getPrincipal() instanceof AuthPrincipal principal)) {
      throw new ForbiddenOperationException("Not authenticated");
    }
    return userRepository
        .findById(principal.userId())
        .map(userMapper::toDomain)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.userId()));
  }

  public boolean teacherTeachesCourse(UUID teacherId, UUID courseId) {
    return courseAssignementRepository.existsByCourse_IdAndTeacher_Id(courseId, teacherId);
  }

  public void assertCanViewStudentGrades(User viewer, UUID studentId) {
    if (viewer.role() == Role.STUDENT && !viewer.id().equals(studentId)) {
      throw new ForbiddenOperationException("A student can only view their own grades");
    }
  }

  public void assertCanViewGrade(User viewer, JGrade grade) {
    if (viewer.role() == Role.ADMIN) {
      return;
    }
    if (viewer.role() == Role.STUDENT) {
      if (!viewer.id().equals(grade.getStudent().getId())) {
        throw new ForbiddenOperationException("A student can only view their own grades");
      }
      return;
    }
    if (viewer.role() == Role.TEACHER) {
      if (!teacherTeachesCourse(viewer.id(), grade.getCourse().getId())) {
        throw new ForbiddenOperationException("A teacher can only view grades of their courses");
      }
      return;
    }
  }

  public void assertCanModifyGrade(User actor, JGrade grade) {
    if (actor.role() == Role.ADMIN) {
      return;
    }
    if (actor.role() == Role.TEACHER) {
      if (!teacherTeachesCourse(actor.id(), grade.getCourse().getId())) {
        throw new ForbiddenOperationException(
            "Only the teacher of the course can modify this grade");
      }
      return;
    }
    throw new ForbiddenOperationException("Only a teacher or an admin can modify grades");
  }

  public void assertAdmin(User actor) {
    if (actor.role() != Role.ADMIN) {
      throw new ForbiddenOperationException("Only an admin can perform this operation");
    }
  }

  public void assertTeacherOrAdmin(User actor) {
    if (actor.role() != Role.ADMIN && actor.role() != Role.TEACHER) {
      throw new ForbiddenOperationException(
          "Only a teacher or an admin can perform this operation");
    }
  }

  public void assertCanAccessCourse(User viewer, UUID courseId) {
    if (viewer.role() == Role.ADMIN) {
      return;
    }
    if (viewer.role() == Role.TEACHER && teacherTeachesCourse(viewer.id(), courseId)) {
      return;
    }
    throw new ForbiddenOperationException("You cannot access this course");
  }

  public void assertStudentSelfOrAdmin(User actor, UUID studentId) {
    if (actor.role() == Role.ADMIN) {
      return;
    }
    if (actor.role() == Role.STUDENT && actor.id().equals(studentId)) {
      return;
    }
    throw new ForbiddenOperationException("A student can only access their own data");
  }
}
