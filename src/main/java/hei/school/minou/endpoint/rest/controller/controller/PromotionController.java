package hei.school.minou.endpoint.rest.controller.controller;

import hei.school.minou.endpoint.rest.controller.dto.Graduate;
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


}
