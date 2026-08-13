package hei.school.minou.repository.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "\"group\"")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JGroup {
  @Id private UUID id;
  private String ref;
}
