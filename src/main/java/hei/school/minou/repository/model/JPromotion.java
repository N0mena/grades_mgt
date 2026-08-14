package hei.school.minou.repository.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "promotion")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JPromotion {
  @Id private UUID id;
  private String ref;
  private String name;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
}
