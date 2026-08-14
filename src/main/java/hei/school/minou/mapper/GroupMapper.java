package hei.school.minou.mapper;

import hei.school.minou.entity.Group;
import hei.school.minou.repository.model.JGroup;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GroupMapper {

  public Group toDomain(JGroup jGroup) {
    if (jGroup == null) {
      return null;
    }
    return Group.builder().id(jGroup.getId()).ref(jGroup.getRef()).build();
  }

  public JGroup toJpa(Group group) {
    if (group == null) {
      return null;
    }
    return new JGroup(group.id(), group.ref());
  }
}
