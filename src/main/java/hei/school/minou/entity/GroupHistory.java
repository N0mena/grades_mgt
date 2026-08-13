package hei.school.minou.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public record GroupHistory(
    UUID id, Group group, User student, LocalDateTime startDate, LocalDateTime endDate) {}
