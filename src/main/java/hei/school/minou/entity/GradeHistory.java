package hei.school.minou.entity;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record GradeHistory(UUID id, Grade grade,Float oldValue, Float newValue, LocalDateTime modifiedAt, String reason, User modifiedBy) {
}
