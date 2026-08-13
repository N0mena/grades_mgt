package hei.school.minou.entity;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record Exam(UUID id, LocalDateTime examDate, Float coefficient, Course course, List<Group> group) {
}
