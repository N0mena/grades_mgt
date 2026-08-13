package hei.school.minou.entity;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record Grade(UUID id, Float value, User student, User teacher, LocalDateTime createdAt, Course course, Exam exam) {
}
