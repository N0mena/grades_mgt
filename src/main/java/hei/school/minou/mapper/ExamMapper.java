package hei.school.minou.mapper;

import hei.school.minou.entity.Exam;
import hei.school.minou.entity.Group;
import hei.school.minou.repository.model.JExam;
import hei.school.minou.repository.model.JGroup;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExamMapper {

  private final CourseMapper courseMapper;
  private final GroupMapper groupMapper;

  public Exam toDomain(JExam jExam) {
    if (jExam == null) {
      return null;
    }
    return Exam.builder()
        .id(jExam.getId())
        .examDate(jExam.getExamDate())
        .coefficient(jExam.getCoefficient())
        .course(courseMapper.toDomain(jExam.getCourse()))
        .group(toDomainGroups(jExam))
        .build();
  }

  private List<Group> toDomainGroups(JExam jExam) {
    if (jExam.getGroups() == null) {
      return null;
    }
    return jExam.getGroups().stream().map(groupMapper::toDomain).toList();
  }

  public JExam toJpa(Exam exam) {
    if (exam == null) {
      return null;
    }
    JExam jExam =
        new JExam(
            exam.id(),
            exam.examDate(),
            exam.coefficient(),
            courseMapper.toJpa(exam.course()),
            null);
    jExam.setGroups(toJpaGroups(exam));
    return jExam;
  }

  private List<JGroup> toJpaGroups(Exam exam) {
    if (exam.group() == null) {
      return null;
    }
    List<JGroup> groups = new ArrayList<>();
    for (Group group : exam.group()) {
      groups.add(groupMapper.toJpa(group));
    }
    return groups;
  }
}
