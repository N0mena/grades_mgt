package hei.school.minou.endpoint.rest.controller.controller;

import hei.school.minou.service.PromotionService;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@AllArgsConstructor
public class PromotionUiController {

  private final PromotionService promotionService;

  @GetMapping("/ui/promotions")
  public String promotions(Model model) {
    model.addAttribute("promotions", promotionService.getAllPromotions());
    return "promotions";
  }

  @GetMapping("/ui/promotions/{id}/graduates")
  public String graduates(@PathVariable UUID id, Model model) {
    model.addAttribute("promotion", promotionService.getPromotionById(id));
    model.addAttribute("graduates", promotionService.getGraduates(id));
    return "promotions/graduates";
  }
}
