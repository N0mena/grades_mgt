package hei.school.minou.endpoint.rest.controller.controller;

import hei.school.minou.endpoint.rest.controller.dto.CourseAssignementRequest;
import hei.school.minou.endpoint.rest.controller.dto.CourseGradeRequest;
import hei.school.minou.endpoint.rest.controller.dto.GroupIdsRequest;
import hei.school.minou.endpoint.rest.controller.dto.TeacherIdsRequest;
import hei.school.minou.entity.Course;
import hei.school.minou.entity.CourseAssignement;
import hei.school.minou.entity.Grade;
import hei.school.minou.entity.Group;
import hei.school.minou.entity.User;
import hei.school.minou.security.SecurityUtils;
import hei.school.minou.service.CourseService;
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
public class CourseController {

  private final CourseService courseService;
  private final GradeService gradeService;
  private final SecurityUtils securityUtils;

  @GetMapping("/courses")
  public List<Course> getCourses() {
    return courseService.getAllCourses();
  }

  @GetMapping("/courses/{id}")
  public Course getCourseById(@PathVariable UUID id) {
    return courseService.getCourseById(id);
  }

  @PostMapping("/courses")
  public Course createCourse(@RequestBody Course course) {
    return courseService.saveCourse(course);
  }

  @GetMapping("/courses/{courseId}/assignements")
  public List<CourseAssignement> getAssignments(@PathVariable UUID courseId) {
    return courseService.getAssignmentsByCourse(courseId);
  }

  @PostMapping("/courses/{courseId}/assignements")
  public CourseAssignement assignCourse(
      @PathVariable UUID courseId, @RequestBody CourseAssignementRequest request) {
    return courseService.assignCourse(courseId, request.teacherId(), request.groupId());
  }

  @PutMapping("/courses/{courseId}/teachers")
  public List<CourseAssignement> setTeachers(
      @PathVariable UUID courseId, @RequestBody TeacherIdsRequest request) {
    return courseService.setTeachers(courseId, request.teacherIds());
  }

  @PutMapping("/courses/{courseId}/groups")
  public List<CourseAssignement> setGroups(
      @PathVariable UUID courseId, @RequestBody GroupIdsRequest request) {
    return courseService.setGroups(courseId, request.groupIds());
  }

  @GetMapping("/courses/{courseId}/groups")
  public List<Group> getCourseGroups(@PathVariable UUID courseId) {
    return courseService.getGroupsForCourse(courseId, securityUtils.currentUser());
  }

  @GetMapping("/courses/{courseId}/students")
  public List<User> getCourseStudents(@PathVariable UUID courseId) {
    return courseService.getStudentsForCourse(courseId, securityUtils.currentUser());
  }

  @GetMapping("/courses/{courseId}/grades")
  public List<Grade> getCourseGrades(@PathVariable UUID courseId) {
    return gradeService.getGradesByCourse(courseId, securityUtils.currentUser());
  }

  @PostMapping("/courses/{courseId}/grades")
  public Grade createCourseGrade(
      @PathVariable UUID courseId, @RequestBody CourseGradeRequest request) {
    User actor = securityUtils.currentUser();
    return gradeService.createGrade(
        request.studentId(), actor.id(), courseId, request.examId(), request.value(), actor);
  }
}
