package hei.school.minou.endpoint.rest.controller.dto;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Builder;

@Builder
public record PromotionResults(
    UUID promotionId, String promotionRef, String promotionName, List<StudentYearResult> students) {

  @Builder
  public record StudentYearResult(
      UUID studentId,
      String firstName,
      String lastName,
      String email,
      Float overallAverage,
      Map<String, Float> courseAverages,
      List<YearResult> years) {}

  @Builder
  public record YearResult(int year, Float average, Map<String, Float> courseAverages) {}
}
