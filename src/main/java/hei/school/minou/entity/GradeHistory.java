package hei.school.minou.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public record GradeHistory(
    UUID id,
    Grade grade,
    Float oldValue,
    Float newValue,
    LocalDateTime modifiedAt,
    String reason,
    User modifiedBy) {}
