package hei.school.minou.endpoint.rest.controller.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.school.minou.endpoint.rest.controller.dto.Graduate;
import hei.school.minou.entity.Promotion;
import hei.school.minou.entity.User;
import hei.school.minou.service.GraduateExcelGenerationService;
import hei.school.minou.service.PromotionService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class PromotionControllerIT {
  @Mock private PromotionService promotionService;
  @Mock private GraduateExcelGenerationService excelGenerationService;

  private PromotionController promotionController;

  @BeforeEach
  void setUp() {
    promotionController = new PromotionController(promotionService, excelGenerationService);
  }

  @Test
  void should_return_all_promotions() {
    Promotion promotion = mock(Promotion.class);
    when(promotionService.getAllPromotions()).thenReturn(List.of(promotion));

    List<Promotion> result = promotionController.getPromotions();

    assertThat(result).containsExactly(promotion);
    verify(promotionService).getAllPromotions();
  }

  @Test
  void should_return_promotion_by_id() {
    UUID id = UUID.randomUUID();
    Promotion promotion = mock(Promotion.class);
    when(promotionService.getPromotionById(id)).thenReturn(promotion);

    Promotion result = promotionController.getPromotionById(id);

    assertThat(result).isEqualTo(promotion);
    verify(promotionService).getPromotionById(id);
  }

  @Test
  void should_create_promotion() {
    Promotion promotion = mock(Promotion.class);
    when(promotionService.savePromotion(promotion)).thenReturn(promotion);

    Promotion result = promotionController.createPromotion(promotion);

    assertThat(result).isEqualTo(promotion);
    verify(promotionService).savePromotion(promotion);
  }

  @Test
  void should_assign_student_to_promotion() {
    UUID promotionId = UUID.randomUUID();
    UUID studentId = UUID.randomUUID();
    User student = mock(User.class);
    when(promotionService.assignStudent(promotionId, studentId)).thenReturn(student);

    User result = promotionController.assignStudent(promotionId, studentId);

    assertThat(result).isEqualTo(student);
    verify(promotionService).assignStudent(promotionId, studentId);
  }


}
