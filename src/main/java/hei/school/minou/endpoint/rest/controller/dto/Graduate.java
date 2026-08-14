package hei.school.minou.endpoint.rest.controller.dto;

import java.util.Map;
import java.util.UUID;
import lombok.Builder;

@Builder
public record Graduate(
    UUID id,
    String firstName,
    String lastName,
    String email,
    Float overallAverage,
    Map<String, Float> courseAverages) {}
