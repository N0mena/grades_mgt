package hei.school.minou.endpoint.rest.controller.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record ExamRequest(
    UUID courseId, LocalDateTime examDate, Float coefficient, List<UUID> groupIds) {}
