package hei.school.minou.service;

import hei.school.minou.entity.GroupHistory;
import hei.school.minou.entity.User;
import hei.school.minou.entity.enums.Role;
import hei.school.minou.mapper.GroupHistoryMapper;
import hei.school.minou.repository.GroupHistoryRepository;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GroupHistoryService {

  private final GroupHistoryRepository groupHistoryRepository;
  private final GroupHistoryMapper groupHistoryMapper;

  public List<GroupHistory> getHistoryByStudent(UUID studentId, User viewer) {
    if (viewer.role() == Role.STUDENT && !viewer.id().equals(studentId)) {
      throw new RuntimeException("A student can only view their own group history");
    }
    if (viewer.role() == Role.TEACHER) {
      throw new RuntimeException("A teacher cannot access group history");
    }
    return groupHistoryRepository.findByStudent_Id(studentId).stream()
        .map(groupHistoryMapper::toDomain)
        .toList();
  }
}
