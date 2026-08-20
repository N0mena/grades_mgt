package hei.school.minou.endpoint.rest.controller.controller;

import hei.school.minou.entity.Course;
import hei.school.minou.security.SecurityUtils;
import hei.school.minou.service.CourseService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/teachers")
@AllArgsConstructor
public class TeacherController {

  private final CourseService courseService;
  private final SecurityUtils securityUtils;

  @GetMapping("/me/courses")
  public List<Course> getMyCourses() {
    return courseService.getCoursesForTeacher(securityUtils.currentUser());
  }
}
