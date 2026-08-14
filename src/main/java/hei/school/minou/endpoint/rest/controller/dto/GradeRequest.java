package hei.school.minou.endpoint.rest.controller.dto;

import java.util.UUID;
import lombok.Builder;

@Builder
public record GradeRequest(
    UUID studentId, UUID teacherId, UUID courseId, UUID examId, Float value) {}
