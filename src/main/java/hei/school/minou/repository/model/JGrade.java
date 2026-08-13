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
@Table(name = "grade")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JGrade {
  @Id private UUID id;
  private Float value;

  @ManyToOne
  @JoinColumn(name = "student_id")
  private JUser student;

  @ManyToOne
  @JoinColumn(name = "teacher_id")
  private JUser teacher;

  private LocalDateTime createdAt;

  @ManyToOne
  @JoinColumn(name = "course_id")
  private JCourse course;

  @ManyToOne
  @JoinColumn(name = "exam_id")
  private JExam exam;
}
