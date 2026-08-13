package hei.school.minou.mapper;

import hei.school.minou.entity.Course;
import hei.school.minou.entity.User;
import hei.school.minou.repository.model.JCourse;
import hei.school.minou.repository.model.JUser;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseMapper {

  private final UserMapper userMapper;

  public Course toDomain(JCourse jCourse) {
    if (jCourse == null) {
      return null;
    }
    return Course.builder()
        .id(jCourse.getId())
        .ref(jCourse.getRef())
        .title(jCourse.getTitle())
        .credit(jCourse.getCredit())
        .teachers(toDomainTeachers(jCourse))
        .build();
  }

  private List<User> toDomainTeachers(JCourse jCourse) {
    if (jCourse.getTeachers() == null) {
      return null;
    }
    return jCourse.getTeachers().stream().map(userMapper::toDomain).toList();
  }

  public JCourse toJpa(Course course) {
    if (course == null) {
      return null;
    }
    JCourse jCourse = new JCourse(course.id(), course.ref(), course.title(), course.credit(), null);
    jCourse.setTeachers(toJpaTeachers(course));
    return jCourse;
  }

  private List<JUser> toJpaTeachers(Course course) {
    if (course.teachers() == null) {
      return null;
    }
    List<JUser> teachers = new ArrayList<>();
    for (User teacher : course.teachers()) {
      teachers.add(userMapper.toJpa(teacher));
    }
    return teachers;
  }
}
