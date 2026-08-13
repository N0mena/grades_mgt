package hei.school.minou.mapper;

import hei.school.minou.entity.GroupHistory;
import hei.school.minou.repository.model.JGroupHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GroupHistoryMapper {

  private final GroupMapper groupMapper;
  private final UserMapper userMapper;

  public GroupHistory toDomain(JGroupHistory jGroupHistory) {
    if (jGroupHistory == null) {
      return null;
    }
    return GroupHistory.builder()
        .id(jGroupHistory.getId())
        .group(groupMapper.toDomain(jGroupHistory.getGroup()))
        .student(userMapper.toDomain(jGroupHistory.getStudent()))
        .startDate(jGroupHistory.getStartDate())
        .endDate(jGroupHistory.getEndDate())
        .build();
  }

  public JGroupHistory toJpa(GroupHistory groupHistory) {
    if (groupHistory == null) {
      return null;
    }
    return new JGroupHistory(
        groupHistory.id(),
        groupMapper.toJpa(groupHistory.group()),
        userMapper.toJpa(groupHistory.student()),
        groupHistory.startDate(),
        groupHistory.endDate());
  }
}
