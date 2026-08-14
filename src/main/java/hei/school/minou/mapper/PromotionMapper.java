package hei.school.minou.mapper;

import hei.school.minou.entity.Promotion;
import hei.school.minou.repository.model.JPromotion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PromotionMapper {

  public Promotion toDomain(JPromotion jPromotion) {
    if (jPromotion == null) {
      return null;
    }
    return Promotion.builder()
        .id(jPromotion.getId())
        .ref(jPromotion.getRef())
        .name(jPromotion.getName())
        .startDate(jPromotion.getStartDate())
        .endDate(jPromotion.getEndDate())
        .build();
  }

  public JPromotion toJpa(Promotion promotion) {
    if (promotion == null) {
      return null;
    }
    return new JPromotion(
        promotion.id(),
        promotion.ref(),
        promotion.name(),
        promotion.startDate(),
        promotion.endDate());
  }
}
