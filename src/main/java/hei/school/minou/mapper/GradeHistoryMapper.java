package hei.school.minou.mapper;

import hei.school.minou.entity.GradeHistory;
import hei.school.minou.repository.model.JGradeHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GradeHistoryMapper {

  private final GradeMapper gradeMapper;
  private final UserMapper userMapper;

  public GradeHistory toDomain(JGradeHistory jGradeHistory) {
    if (jGradeHistory == null) {
      return null;
    }
    return GradeHistory.builder()
        .id(jGradeHistory.getId())
        .grade(gradeMapper.toDomain(jGradeHistory.getGrade()))
        .oldValue(jGradeHistory.getOldValue())
        .newValue(jGradeHistory.getNewValue())
        .modifiedAt(jGradeHistory.getModifiedAt())
        .reason(jGradeHistory.getReason())
        .modifiedBy(userMapper.toDomain(jGradeHistory.getModifiedBy()))
        .build();
  }

  public JGradeHistory toJpa(GradeHistory gradeHistory) {
    if (gradeHistory == null) {
      return null;
    }
    return new JGradeHistory(
        gradeHistory.id(),
        gradeMapper.toJpa(gradeHistory.grade()),
        gradeHistory.oldValue(),
        gradeHistory.newValue(),
        gradeHistory.modifiedAt(),
        gradeHistory.reason(),
        userMapper.toJpa(gradeHistory.modifiedBy()));
  }
}
