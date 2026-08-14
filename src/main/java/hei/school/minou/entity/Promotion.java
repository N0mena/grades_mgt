package hei.school.minou.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public record Promotion(
    UUID id, String ref, String name, LocalDateTime startDate, LocalDateTime endDate) {}
