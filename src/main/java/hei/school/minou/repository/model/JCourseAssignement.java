package hei.school.minou.repository.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "course_assignement")
@Getter
@Setter
@AllArgsConstructor
public class JCourseAssignement {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  private String ref;
  private String title;
  private Integer credit;

  @ManyToMany
  @JoinTable(
      name = "course_teacher",
      joinColumns = @JoinColumn(name = "course_id"),
      inverseJoinColumns = @JoinColumn(name = "teacher_id"))
  private List<JUser> teachers;

  @ManyToMany
  @JoinTable(
      name = "course_group",
      joinColumns = @JoinColumn(name = "course_id"),
      inverseJoinColumns = @JoinColumn(name = "group_id"))
  private List<JGroup> groups;
}
