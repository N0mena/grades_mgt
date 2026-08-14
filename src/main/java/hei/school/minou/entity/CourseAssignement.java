package hei.school.minou.entity;

import java.util.UUID;
import lombok.Builder;

@Builder
public record CourseAssignement(UUID id, Course course, User teacher, Group group) {}
