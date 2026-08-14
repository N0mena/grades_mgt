package hei.school.minou.mapper;

import hei.school.minou.entity.User;
import hei.school.minou.repository.model.JUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

  private final PromotionMapper promotionMapper;

  public User toDomain(JUser jUser) {
    if (jUser == null) {
      return null;
    }
    return User.builder()
        .id(jUser.getId())
        .firstName(jUser.getFirstName())
        .lastName(jUser.getLastName())
        .role(jUser.getRole())
        .email(jUser.getEmail())
        .password(jUser.getPassword())
        .promotion(promotionMapper.toDomain(jUser.getPromotion()))
        .build();
  }

  public JUser toJpa(User user) {
    if (user == null) {
      return null;
    }
    return new JUser(
        user.id(),
        user.firstName(),
        user.lastName(),
        user.role(),
        user.email(),
        user.password(),
        promotionMapper.toJpa(user.promotion()));
  }
}
