package hei.school.minou.endpoint.rest.controller.controller;

import hei.school.minou.endpoint.rest.controller.dto.Graduate;
import hei.school.minou.endpoint.rest.controller.dto.PromotionResults;
import hei.school.minou.entity.Promotion;
import hei.school.minou.entity.User;
import hei.school.minou.service.GraduateExcelGenerationService;
import hei.school.minou.service.PromotionService;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class PromotionController {

  private final PromotionService promotionService;
  private final GraduateExcelGenerationService excelGenerationService;

  @GetMapping("/promotions")
  public List<Promotion> getPromotions() {
    return promotionService.getAllPromotions();
  }

  @GetMapping("/promotions/{id}")
  public Promotion getPromotionById(@PathVariable UUID id) {
    return promotionService.getPromotionById(id);
  }

  @PostMapping("/promotions")
  public Promotion createPromotion(@RequestBody Promotion promotion) {
    return promotionService.savePromotion(promotion);
  }

  @PutMapping("/promotions/{id}/students/{studentId}")
  public User assignStudent(@PathVariable UUID id, @PathVariable UUID studentId) {
    return promotionService.assignStudent(id, studentId);
  }

  @GetMapping("/promotions/{id}/graduates")
  public List<Graduate> getGraduates(@PathVariable UUID id) {
    return promotionService.getGraduates(id);
  }

  @GetMapping("/promotions/{id}/results")
  public PromotionResults getResults(@PathVariable UUID id) {
    return promotionService.getResults(id);
  }

  @GetMapping("/promotions/{id}/graduates/export")
  public ResponseEntity<byte[]> exportGraduates(@PathVariable UUID id) {
    Promotion promotion = promotionService.getPromotionById(id);
    List<Graduate> graduates = promotionService.getGraduates(id);
    byte[] file = excelGenerationService.generate(promotion, graduates);
    String ref = promotion.ref() != null ? promotion.ref() : promotion.id().toString();
    return ResponseEntity.ok()
        .header(
            HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"diplomes_" + ref + ".xlsx\"")
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(file);
  }
}
