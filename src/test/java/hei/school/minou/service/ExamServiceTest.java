package hei.school.minou.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.school.minou.entity.Exam;
import hei.school.minou.exception.ResourceNotFoundException;
import hei.school.minou.mapper.ExamMapper;
import hei.school.minou.repository.CourseRepository;
import hei.school.minou.repository.ExamRepository;
import hei.school.minou.repository.GroupRepository;
import hei.school.minou.repository.model.JCourse;
import hei.school.minou.repository.model.JExam;
import hei.school.minou.repository.model.JGroup;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ExamServiceTest {

  private ExamRepository examRepository;
  private CourseRepository courseRepository;
  private GroupRepository groupRepository;
  private ExamMapper examMapper;
  private ExamService examService;

  @BeforeEach
  void setUp() {
    examRepository = mock(ExamRepository.class);
    courseRepository = mock(CourseRepository.class);
    groupRepository = mock(GroupRepository.class);
    examMapper = mock(ExamMapper.class);
    examService = new ExamService(examRepository, courseRepository, groupRepository, examMapper);
  }

  @Nested
  class CreateExam {

    @Test
    void unknownCourse_throwsNotFound() {
      UUID courseId = UUID.randomUUID();
      when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

      assertThatThrownBy(
              () -> examService.createExam(courseId, LocalDateTime.now(), 1.0f, List.of()))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessage("Course not found: " + courseId);
    }

    @Test
    void validExam_isSaved() {
      UUID courseId = UUID.randomUUID();
      JCourse course = new JCourse(courseId, "P", "T", 1);
      LocalDateTime date = LocalDateTime.now();
      when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
      JExam saved = new JExam(UUID.randomUUID(), date, 2.0f, course, null);
      when(examRepository.save(any(JExam.class))).thenReturn(saved);
      when(examMapper.toDomain(saved))
          .thenReturn(Exam.builder().id(saved.getId()).coefficient(2.0f).build());

      Exam result = examService.createExam(courseId, date, 2.0f, null);

      assertThat(result.id()).isEqualTo(saved.getId());
      assertThat(result.coefficient()).isEqualTo(2.0f);
      verify(examRepository).save(any(JExam.class));
    }

    @Test
    void withGroups_loadsAndAttachesThem() {
      UUID courseId = UUID.randomUUID();
      UUID groupId = UUID.randomUUID();
      JCourse course = new JCourse(courseId, "P", "T", 1);
      JGroup group = new JGroup(groupId, "G1");
      when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
      when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
      JExam saved = new JExam(UUID.randomUUID(), LocalDateTime.now(), 1.0f, course, List.of(group));
      when(examRepository.save(any(JExam.class))).thenReturn(saved);
      when(examMapper.toDomain(saved)).thenReturn(Exam.builder().id(saved.getId()).build());

      examService.createExam(courseId, LocalDateTime.now(), 1.0f, List.of(groupId));

      verify(groupRepository).findById(groupId);
    }

    @Test
    void unknownGroup_throwsNotFound() {
      UUID courseId = UUID.randomUUID();
      UUID groupId = UUID.randomUUID();
      when(courseRepository.findById(courseId))
          .thenReturn(Optional.of(new JCourse(courseId, "P", "T", 1)));
      when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

      assertThatThrownBy(
              () -> examService.createExam(courseId, LocalDateTime.now(), 1.0f, List.of(groupId)))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessage("Group not found: " + groupId);
    }
  }

  @Nested
  class GetExamById {

    @Test
    void unknownExam_throwsNotFound() {
      UUID id = UUID.randomUUID();
      when(examRepository.findById(id)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> examService.getExamById(id))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessage("Exam not found: " + id);
    }

    @Test
    void knownExam_isReturned() {
      JExam jExam = new JExam(UUID.randomUUID(), LocalDateTime.now(), 1.0f, null, null);
      Exam expected = Exam.builder().id(jExam.getId()).build();
      when(examRepository.findById(jExam.getId())).thenReturn(Optional.of(jExam));
      when(examMapper.toDomain(jExam)).thenReturn(expected);

      Exam result = examService.getExamById(jExam.getId());

      assertThat(result).isEqualTo(expected);
    }
  }

  @Nested
  class GetAllExams {

    @Test
    void returnsAllMappedExams() {
      JExam jExam = new JExam(UUID.randomUUID(), LocalDateTime.now(), 1.0f, null, null);
      when(examRepository.findAll()).thenReturn(List.of(jExam));
      when(examMapper.toDomain(jExam)).thenReturn(Exam.builder().id(jExam.getId()).build());

      List<Exam> exams = examService.getAllExams();

      assertThat(exams).hasSize(1);
    }
  }

  @Nested
  class GetExamsByCourse {

    @Test
    void returnsExamsForCourse() {
      UUID courseId = UUID.randomUUID();
      JExam jExam = new JExam(UUID.randomUUID(), LocalDateTime.now(), 1.0f, null, null);
      when(examRepository.findByCourse_Id(courseId)).thenReturn(List.of(jExam));
      when(examMapper.toDomain(jExam)).thenReturn(Exam.builder().id(jExam.getId()).build());

      List<Exam> exams = examService.getExamsByCourse(courseId);

      assertThat(exams).hasSize(1);
    }
  }

  @Nested
  class LinkGroups {

    @Test
    void unknownExam_throwsNotFound() {
      UUID examId = UUID.randomUUID();
      when(examRepository.findById(examId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> examService.linkGroups(examId, List.of()))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessage("Exam not found: " + examId);
    }

    @Test
    void linksGroupsToExam() {
      UUID examId = UUID.randomUUID();
      UUID groupId = UUID.randomUUID();
      JExam jExam = new JExam(examId, LocalDateTime.now(), 1.0f, null, null);
      JGroup group = new JGroup(groupId, "G1");
      when(examRepository.findById(examId)).thenReturn(Optional.of(jExam));
      when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
      when(examRepository.save(jExam)).thenReturn(jExam);

      examService.linkGroups(examId, List.of(groupId));

      assertThat(jExam.getGroups()).containsExactly(group);
      verify(examRepository).save(jExam);
    }
  }
}
