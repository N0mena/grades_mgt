package hei.school.minou.endpoint.rest.controller.dto;

import lombok.Builder;

@Builder
public record UpdateGradeRequest(Float value, String reason) {}
