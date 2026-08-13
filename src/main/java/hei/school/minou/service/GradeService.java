package hei.school.minou.service;

import hei.school.minou.entity.Grade;
import hei.school.minou.entity.User;
import hei.school.minou.entity.enums.Role;
import hei.school.minou.mapper.GradeHistoryMapper;
import hei.school.minou.mapper.GradeMapper;
import hei.school.minou.repository.CourseAssignementRepository;
import hei.school.minou.repository.CourseRepository;
import hei.school.minou.repository.ExamRepository;
import hei.school.minou.repository.GradeHistoryRepository;
import hei.school.minou.repository.GradeRepository;
import hei.school.minou.repository.UserRepository;
import hei.school.minou.repository.model.JCourse;
import hei.school.minou.repository.model.JExam;
import hei.school.minou.repository.model.JGrade;
import hei.school.minou.repository.model.JGradeHistory;
import hei.school.minou.repository.model.JUser;
import hei.school.minou.security.SecurityUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class GradeService {

  private final GradeRepository gradeRepository;
  private final GradeHistoryRepository gradeHistoryRepository;
  private final UserRepository userRepository;
  private final CourseRepository courseRepository;
  private final ExamRepository examRepository;
  private final CourseAssignementRepository courseAssignementRepository;
  private final SecurityUtils accessControlService;
  private final GradeMapper gradeMapper;
  private final GradeHistoryMapper gradeHistoryMapper;

  public List<Grade> getGradesByStudent(UUID studentId, User viewer) {
    accessControlService.assertCanViewStudentGrades(viewer, studentId);
    List<JGrade> grades = gradeRepository.findByStudent_Id(studentId);
    if (viewer.role() == Role.TEACHER) {
      List<UUID> courseIds = coursesTaughtBy(viewer.id());
      return grades.stream()
          .filter(grade -> courseIds.contains(grade.getCourse().getId()))
          .map(gradeMapper::toDomain)
          .toList();
    }
    return grades.stream().map(gradeMapper::toDomain).toList();
  }

  public List<Grade> getGradesByCourse(UUID courseId, User viewer) {
    if (viewer.role() == Role.STUDENT) {
      throw new RuntimeException("A student cannot view all grades of a course");
    }
    if (viewer.role() == Role.TEACHER
        && !accessControlService.teacherTeachesCourse(viewer.id(), courseId)) {
      throw new RuntimeException("A teacher can only view grades of their courses");
    }
    return gradeRepository.findByCourse_Id(courseId).stream().map(gradeMapper::toDomain).toList();
  }

  public Grade getGradeById(UUID gradeId, User viewer) {
    JGrade jGrade =
        gradeRepository
            .findById(gradeId)
            .orElseThrow(() -> new RuntimeException("Grade not found: " + gradeId));
    accessControlService.assertCanViewGrade(viewer, jGrade);
    return gradeMapper.toDomain(jGrade);
  }

  @Transactional
  public Grade createGrade(
      UUID studentId, UUID teacherId, UUID courseId, UUID examId, Float value, User actor) {
    JCourse course =
        courseRepository
            .findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found: " + courseId));
    if (actor.role() != Role.ADMIN
        && !accessControlService.teacherTeachesCourse(actor.id(), courseId)) {
      throw new RuntimeException("Only the teacher of the course or an admin can grade it");
    }
    JUser student =
        userRepository
            .findById(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));
    JUser teacher =
        userRepository
            .findById(teacherId)
            .orElseThrow(() -> new RuntimeException("Teacher not found: " + teacherId));
    JExam exam =
        examId == null
            ? null
            : examRepository
                .findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found: " + examId));

    JGrade jGrade =
        new JGrade(UUID.randomUUID(), value, student, teacher, LocalDateTime.now(), course, exam);
    return gradeMapper.toDomain(gradeRepository.save(jGrade));
  }

  @Transactional
  public Grade updateGrade(UUID gradeId, Float newValue, String reason, User actor) {
    if (reason == null || reason.isBlank()) {
      throw new RuntimeException("A reason is required to modify a grade");
    }
    JGrade jGrade =
        gradeRepository
            .findById(gradeId)
            .orElseThrow(() -> new RuntimeException("Grade not found: " + gradeId));
    accessControlService.assertCanModifyGrade(actor, jGrade);

    Float oldValue = jGrade.getValue();
    jGrade.setValue(newValue);
    JGrade saved = gradeRepository.save(jGrade);

    JUser modifiedBy =
        userRepository
            .findById(actor.id())
            .orElseThrow(() -> new RuntimeException("User not found: " + actor.id()));
    JGradeHistory history =
        new JGradeHistory(
            UUID.randomUUID(), saved, oldValue, newValue, LocalDateTime.now(), reason, modifiedBy);
    gradeHistoryRepository.save(history);

    return gradeMapper.toDomain(saved);
  }

  private List<UUID> coursesTaughtBy(UUID teacherId) {
    return courseAssignementRepository.findByTeacher_Id(teacherId).stream()
        .map(assignment -> assignment.getCourse().getId())
        .distinct()
        .toList();
  }
}
