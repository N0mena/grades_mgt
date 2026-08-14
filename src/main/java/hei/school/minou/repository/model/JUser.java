package hei.school.minou.repository.model;

import hei.school.minou.entity.enums.Role;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "\"user\"")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JUser {
  @Id private UUID id;
  private String firstName;
  private String lastName;

  @Enumerated(EnumType.STRING)
  private Role role;

  private String email;
  private String password;

  @ManyToOne
  @JoinColumn(name = "promotion_id")
  private JPromotion promotion;
}
