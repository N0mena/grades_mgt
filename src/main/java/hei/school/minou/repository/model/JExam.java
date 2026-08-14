package hei.school.minou.repository.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "exam")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JExam {

  @Id private UUID id;
  private LocalDateTime examDate;
  private Float coefficient;

  @ManyToOne
  @JoinColumn(name = "course_id")
  private JCourse course;

  @ManyToMany
  @JoinTable(
      name = "exam_group",
      joinColumns = @JoinColumn(name = "exam_id"),
      inverseJoinColumns = @JoinColumn(name = "group_id"))
  private List<JGroup> groups;
}
