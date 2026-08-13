package hei.school.minou.entity;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record GroupHistory(UUID id, Group group, User student, LocalDateTime startDate, LocalDateTime endDate) {
}
