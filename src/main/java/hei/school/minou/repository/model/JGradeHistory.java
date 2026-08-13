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
@Table(name = "grade_history")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JGradeHistory {

  @Id private UUID id;

  @ManyToOne
  @JoinColumn(name = "grade_id")
  private JGrade grade;

  private Float oldValue;
  private Float newValue;
  private LocalDateTime modifiedAt;
  private String reason;

  @ManyToOne
  @JoinColumn(name = "modified_by")
  private JUser modifiedBy;
}
