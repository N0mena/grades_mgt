package hei.school.minou.endpoint.rest.controller.controller;

import hei.school.minou.endpoint.rest.controller.dto.CourseAssignementRequest;
import hei.school.minou.entity.Course;
import hei.school.minou.entity.CourseAssignement;
import hei.school.minou.entity.Grade;
import hei.school.minou.security.SecurityUtils;
import hei.school.minou.service.CourseService;
import hei.school.minou.service.GradeService;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

  @GetMapping("/courses/{courseId}/grades")
  public List<Grade> getCourseGrades(@PathVariable UUID courseId) {
    return gradeService.getGradesByCourse(courseId, securityUtils.currentUser());
  }
}
