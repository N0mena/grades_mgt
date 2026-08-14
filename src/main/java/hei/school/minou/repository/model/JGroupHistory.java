package hei.school.minou.repository.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "group_history")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JGroupHistory {
  @Id private UUID id;

  @ManyToOne
  @JoinColumn(name = "group_id")
  private JGroup group;

  @ManyToOne
  @JoinColumn(name = "student_id")
  private JUser student;

  private LocalDateTime startDate;
  private LocalDateTime endDate;
}
