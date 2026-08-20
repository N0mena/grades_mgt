package hei.school.minou.service;

import hei.school.minou.entity.Group;
import hei.school.minou.entity.GroupHistory;
import hei.school.minou.entity.User;
import hei.school.minou.exception.ResourceNotFoundException;
import hei.school.minou.mapper.GroupHistoryMapper;
import hei.school.minou.mapper.GroupMapper;
import hei.school.minou.mapper.UserMapper;
import hei.school.minou.repository.GroupHistoryRepository;
import hei.school.minou.repository.GroupRepository;
import hei.school.minou.repository.UserRepository;
import hei.school.minou.repository.model.JGroup;
import hei.school.minou.repository.model.JGroupHistory;
import hei.school.minou.repository.model.JUser;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class GroupService {

  private final GroupRepository groupRepository;
  private final UserRepository userRepository;
  private final GroupHistoryRepository groupHistoryRepository;
  private final GroupMapper groupMapper;
  private final GroupHistoryMapper groupHistoryMapper;
  private final UserMapper userMapper;

  public Group saveGroup(Group group) {
    Group toSave =
        group.id() != null
            ? group
            : Group.builder().id(UUID.randomUUID()).ref(group.ref()).build();
    return groupMapper.toDomain(groupRepository.save(groupMapper.toJpa(toSave)));
  }

  public Group getCurrentGroup(UUID studentId) {
    return groupHistoryRepository.findByStudent_IdAndEndDateIsNull(studentId).stream()
        .findFirst()
        .map(history -> groupMapper.toDomain(history.getGroup()))
        .orElseThrow(
            () -> new ResourceNotFoundException("No current group for student: " + studentId));
  }

  public Group getGroupById(UUID id) {
    JGroup jGroup =
        groupRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Group not found: " + id));
    return groupMapper.toDomain(jGroup);
  }

  public List<Group> getAllGroups() {
    return groupRepository.findAll().stream().map(groupMapper::toDomain).toList();
  }

  @Transactional
  public GroupHistory assignStudent(UUID studentId, UUID groupId) {
    JUser student =
        userRepository
            .findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));
    JGroup group =
        groupRepository
            .findById(groupId)
            .orElseThrow(() -> new ResourceNotFoundException("Group not found: " + groupId));
    JGroupHistory jGroupHistory =
        new JGroupHistory(UUID.randomUUID(), group, student, LocalDateTime.now(), null);
    return groupHistoryMapper.toDomain(groupHistoryRepository.save(jGroupHistory));
  }

  @Transactional
  public GroupHistory moveStudent(UUID studentId, UUID newGroupId) {
    List<JGroupHistory> openHistories =
        groupHistoryRepository.findByStudent_IdAndEndDateIsNull(studentId);
    openHistories.forEach(history -> history.setEndDate(LocalDateTime.now()));
    groupHistoryRepository.saveAll(openHistories);
    return assignStudent(studentId, newGroupId);
  }

  public List<User> getStudentsInGroup(UUID groupId) {
    return groupHistoryRepository.findByGroup_IdAndEndDateIsNull(groupId).stream()
        .map(history -> userMapper.toDomain(history.getStudent()))
        .toList();
  }
}
