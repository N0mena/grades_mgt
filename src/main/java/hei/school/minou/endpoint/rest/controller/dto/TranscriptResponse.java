package hei.school.minou.endpoint.rest.controller.dto;

import hei.school.minou.entity.Grade;
import hei.school.minou.entity.Promotion;
import hei.school.minou.entity.User;
import java.util.List;
import java.util.Map;
import lombok.Builder;

@Builder
public record TranscriptResponse(
    User student,
    Promotion promotion,
    List<Grade> grades,
    Map<String, Float> courseAverages,
    Float overallAverage) {}
