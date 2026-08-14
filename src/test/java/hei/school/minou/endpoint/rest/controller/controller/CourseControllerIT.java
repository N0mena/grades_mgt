package hei.school.minou.endpoint.rest.controller.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.school.minou.endpoint.rest.controller.dto.CourseAssignementRequest;
import hei.school.minou.entity.Course;
import hei.school.minou.entity.CourseAssignement;
import hei.school.minou.entity.Grade;
import hei.school.minou.entity.User;
import hei.school.minou.security.SecurityUtils;
import hei.school.minou.service.CourseService;
import hei.school.minou.service.GradeService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CourseControllerIT {

  @Mock private CourseService courseService;
  @Mock private GradeService gradeService;
  @Mock private SecurityUtils securityUtils;

  private CourseController courseController;

  @BeforeEach
  void setUp() {
    courseController = new CourseController(courseService, gradeService, securityUtils);
  }

  @Test
  void should_return_all_courses() {
    Course course = mock(Course.class);
    when(courseService.getAllCourses()).thenReturn(List.of(course));

    List<Course> result = courseController.getCourses();

    assertThat(result).containsExactly(course);
    verify(courseService).getAllCourses();
  }

  @Test
  void should_return_course_by_id() {
    UUID courseId = UUID.randomUUID();
    Course course = mock(Course.class);
    when(courseService.getCourseById(courseId)).thenReturn(course);

    Course result = courseController.getCourseById(courseId);

    assertThat(result).isEqualTo(course);
    verify(courseService).getCourseById(courseId);
  }

  @Test
  void should_create_course() {
    Course courseToCreate = mock(Course.class);
    Course savedCourse = mock(Course.class);
    when(courseService.saveCourse(courseToCreate)).thenReturn(savedCourse);

    Course result = courseController.createCourse(courseToCreate);

    assertThat(result).isEqualTo(savedCourse);
    verify(courseService).saveCourse(courseToCreate);
  }

  @Test
  void should_return_assignments_for_course() {
    UUID courseId = UUID.randomUUID();
    CourseAssignement assignment = mock(CourseAssignement.class);
    when(courseService.getAssignmentsByCourse(courseId)).thenReturn(List.of(assignment));

    List<CourseAssignement> result = courseController.getAssignments(courseId);

    assertThat(result).containsExactly(assignment);
    verify(courseService).getAssignmentsByCourse(courseId);
  }

  @Test
  void should_return_empty_assignments_when_none_exist() {
    UUID courseId = UUID.randomUUID();
    when(courseService.getAssignmentsByCourse(courseId)).thenReturn(List.of());

    List<CourseAssignement> result = courseController.getAssignments(courseId);

    assertThat(result).isEmpty();
    verify(courseService).getAssignmentsByCourse(courseId);
  }

  @Test
  void should_assign_course_to_teacher_and_group() {
    UUID courseId = UUID.randomUUID();
    UUID teacherId = UUID.randomUUID();
    UUID groupId = UUID.randomUUID();
    CourseAssignementRequest request = new CourseAssignementRequest(teacherId, groupId);
    CourseAssignement createdAssignment = mock(CourseAssignement.class);

    when(courseService.assignCourse(courseId, teacherId, groupId)).thenReturn(createdAssignment);

    CourseAssignement result = courseController.assignCourse(courseId, request);

    assertThat(result).isEqualTo(createdAssignment);
    verify(courseService).assignCourse(courseId, teacherId, groupId);
  }

  @Test
  void should_return_grades_for_course_using_current_user() {
    UUID courseId = UUID.randomUUID();
    User currentUser = mock(User.class);
    Grade grade = mock(Grade.class);

    when(securityUtils.currentUser()).thenReturn(currentUser);
    when(gradeService.getGradesByCourse(courseId, currentUser)).thenReturn(List.of(grade));

    List<Grade> result = courseController.getCourseGrades(courseId);

    assertThat(result).containsExactly(grade);
    verify(gradeService).getGradesByCourse(courseId, currentUser);
  }
}
