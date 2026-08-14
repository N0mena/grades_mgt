package hei.school.minou.repository.model;

import jakarta.persistence.Entity;
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
@Table(name = "course_assignement")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JCourseAssignement {

  @Id private UUID id;

  @ManyToOne
  @JoinColumn(name = "course_id")
  private JCourse course;

  @ManyToOne
  @JoinColumn(name = "teacher_id")
  private JUser teacher;

  @ManyToOne
  @JoinColumn(name = "group_id")
  private JGroup group;
}
