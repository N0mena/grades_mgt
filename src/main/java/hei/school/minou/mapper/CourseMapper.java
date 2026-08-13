package hei.school.minou.mapper;

import hei.school.minou.entity.Course;
import hei.school.minou.entity.CourseAssignement;
import hei.school.minou.repository.model.JCourse;
import hei.school.minou.repository.model.JCourseAssignement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseMapper {

  private final UserMapper userMapper;
  private final GroupMapper groupMapper;

  public Course toDomain(JCourse jCourse) {
    if (jCourse == null) {
      return null;
    }
    return Course.builder()
        .id(jCourse.getId())
        .ref(jCourse.getRef())
        .title(jCourse.getTitle())
        .credit(jCourse.getCredit())
        .build();
  }

  public JCourse toJpa(Course course) {
    if (course == null) {
      return null;
    }
    return new JCourse(course.id(), course.ref(), course.title(), course.credit());
  }

  public CourseAssignement toDomain(JCourseAssignement jCourseAssignement) {
    if (jCourseAssignement == null) {
      return null;
    }
    return CourseAssignement.builder()
        .id(jCourseAssignement.getId())
        .course(toDomain(jCourseAssignement.getCourse()))
        .teacher(userMapper.toDomain(jCourseAssignement.getTeacher()))
        .group(groupMapper.toDomain(jCourseAssignement.getGroup()))
        .build();
  }

  public JCourseAssignement toJpa(CourseAssignement courseAssignement) {
    if (courseAssignement == null) {
      return null;
    }
    return new JCourseAssignement(
        courseAssignement.id(),
        toJpa(courseAssignement.course()),
        userMapper.toJpa(courseAssignement.teacher()),
        groupMapper.toJpa(courseAssignement.group()));
  }
}
