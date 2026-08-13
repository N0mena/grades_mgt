package hei.school.minou.mapper;

import hei.school.minou.entity.Grade;
import hei.school.minou.repository.model.JGrade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GradeMapper {

  private final UserMapper userMapper;
  private final CourseMapper courseMapper;
  private final ExamMapper examMapper;

  public Grade toDomain(JGrade jGrade) {
    if (jGrade == null) {
      return null;
    }
    return Grade.builder()
        .id(jGrade.getId())
        .value(jGrade.getValue())
        .student(userMapper.toDomain(jGrade.getStudent()))
        .teacher(userMapper.toDomain(jGrade.getTeacher()))
        .createdAt(jGrade.getCreatedAt())
        .course(courseMapper.toDomain(jGrade.getCourse()))
        .exam(examMapper.toDomain(jGrade.getExam()))
        .build();
  }

  public JGrade toJpa(Grade grade) {
    if (grade == null) {
      return null;
    }
    return new JGrade(
        grade.id(),
        grade.value(),
        userMapper.toJpa(grade.student()),
        userMapper.toJpa(grade.teacher()),
        grade.createdAt(),
        courseMapper.toJpa(grade.course()),
        examMapper.toJpa(grade.exam()));
  }
}
