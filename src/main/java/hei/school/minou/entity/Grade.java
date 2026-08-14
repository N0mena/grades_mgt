package hei.school.minou.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public record Grade(
    UUID id,
    Float value,
    User student,
    User teacher,
    LocalDateTime createdAt,
    Course course,
    Exam exam) {}
