package hei.school.minou.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.school.minou.endpoint.rest.controller.dto.Graduate;
import hei.school.minou.entity.Promotion;
import hei.school.minou.entity.User;
import hei.school.minou.entity.enums.Role;
import hei.school.minou.exception.ResourceNotFoundException;
import hei.school.minou.mapper.PromotionMapper;
import hei.school.minou.mapper.UserMapper;
import hei.school.minou.repository.CourseAssignementRepository;
import hei.school.minou.repository.ExamRepository;
import hei.school.minou.repository.GradeRepository;
import hei.school.minou.repository.GroupHistoryRepository;
import hei.school.minou.repository.PromotionRepository;
import hei.school.minou.repository.UserRepository;
import hei.school.minou.repository.model.JCourse;
import hei.school.minou.repository.model.JCourseAssignement;
import hei.school.minou.repository.model.JExam;
import hei.school.minou.repository.model.JGrade;
import hei.school.minou.repository.model.JGroup;
import hei.school.minou.repository.model.JGroupHistory;
import hei.school.minou.repository.model.JPromotion;
import hei.school.minou.repository.model.JUser;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PromotionServiceTest {

  private PromotionRepository promotionRepository;
  private UserRepository userRepository;
  private GroupHistoryRepository groupHistoryRepository;
  private GradeRepository gradeRepository;
  private CourseAssignementRepository courseAssignementRepository;
  private ExamRepository examRepository;
  private PromotionMapper promotionMapper;
  private UserMapper userMapper;
  private PromotionService promotionService;

  @BeforeEach
  void setUp() {
    promotionRepository = mock(PromotionRepository.class);
    userRepository = mock(UserRepository.class);
    groupHistoryRepository = mock(GroupHistoryRepository.class);
    gradeRepository = mock(GradeRepository.class);
    courseAssignementRepository = mock(CourseAssignementRepository.class);
    examRepository = mock(ExamRepository.class);
    promotionMapper = mock(PromotionMapper.class);
    userMapper = mock(UserMapper.class);
    promotionService =
        new PromotionService(
            promotionRepository,
            userRepository,
            groupHistoryRepository,
            gradeRepository,
            courseAssignementRepository,
            examRepository,
            promotionMapper,
            userMapper);
  }

  @Nested
  class SavePromotion {

    @Test
    void savesAndMapsBack() {
      Promotion promotion = Promotion.builder().id(UUID.randomUUID()).ref("P2025").build();
      JPromotion jPromotion = promotion(promotion.id());
      when(promotionMapper.toJpa(promotion)).thenReturn(jPromotion);
      when(promotionRepository.save(jPromotion)).thenReturn(jPromotion);
      when(promotionMapper.toDomain(jPromotion)).thenReturn(promotion);

      Promotion result = promotionService.savePromotion(promotion);

      assertThat(result).isEqualTo(promotion);
      verify(promotionRepository).save(jPromotion);
    }
  }

  @Nested
  class GetPromotionById {

    @Test
    void unknownPromotion_throwsNotFound() {
      UUID id = UUID.randomUUID();
      when(promotionRepository.findById(id)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> promotionService.getPromotionById(id))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessage("Promotion not found: " + id);
    }
  }

  @Nested
  class GetAllPromotions {

    @Test
    void returnsAllMappedPromotions() {
      JPromotion jPromotion = promotion(UUID.randomUUID());
      Promotion promotion = Promotion.builder().id(jPromotion.getId()).build();
      when(promotionRepository.findAll()).thenReturn(List.of(jPromotion));
      when(promotionMapper.toDomain(jPromotion)).thenReturn(promotion);

      List<Promotion> result = promotionService.getAllPromotions();

      assertThat(result).containsExactly(promotion);
    }
  }

  @Nested
  class AssignStudent {

    @Test
    void setsPromotionAndSaves() {
      UUID promotionId = UUID.randomUUID();
      UUID studentId = UUID.randomUUID();
      JPromotion jPromotion = promotion(promotionId);
      JUser student = student(studentId, "A", "B");
      when(promotionRepository.findById(promotionId)).thenReturn(Optional.of(jPromotion));
      when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
      when(userRepository.save(student)).thenReturn(student);
      when(userMapper.toDomain(student)).thenReturn(User.builder().id(studentId).build());

      User result = promotionService.assignStudent(promotionId, studentId);

      assertThat(student.getPromotion()).isEqualTo(jPromotion);
      assertThat(result.id()).isEqualTo(studentId);
    }

    @Test
    void unknownPromotion_throwsNotFound() {
      UUID promotionId = UUID.randomUUID();
      when(promotionRepository.findById(promotionId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> promotionService.assignStudent(promotionId, UUID.randomUUID()))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessage("Promotion not found: " + promotionId);
    }
  }

  @Nested
  class GetGraduates {

    @Test
    void studentWithAllCoursesAboveTen_isGraduate() {
      UUID promotionId = UUID.randomUUID();
      UUID groupId = UUID.randomUUID();
      UUID courseId = UUID.randomUUID();
      JUser student = student(UUID.randomUUID(), "Jean", "Dupont");
      JCourse course = course(courseId, "Maths", 3);
      stubPromotionLookup(promotionId);
      when(userRepository.findByRoleAndPromotion_Id(Role.STUDENT, promotionId))
          .thenReturn(List.of(student));
      when(groupHistoryRepository.findByStudent_Id(student.getId()))
          .thenReturn(List.of(history(student.getId(), groupId)));
      when(courseAssignementRepository.findByGroup_IdIn(List.of(groupId)))
          .thenReturn(List.of(assignment(course, groupId)));
      when(examRepository.findByGroups_IdIn(List.of(groupId))).thenReturn(List.of());
      when(gradeRepository.findByStudent_IdAndCourse_Id(student.getId(), courseId))
          .thenReturn(List.of(grade(student.getId(), course, 12f)));

      List<Graduate> graduates = promotionService.getGraduates(promotionId);

      assertThat(graduates).hasSize(1);
      Graduate graduate = graduates.get(0);
      assertThat(graduate.firstName()).isEqualTo("Jean");
      assertThat(graduate.lastName()).isEqualTo("Dupont");
      assertThat(graduate.courseAverages()).containsEntry("Maths", 12.0f);
      assertThat(graduate.overallAverage()).isEqualTo(12.0f);
    }

    @Test
    void studentWithOneCourseBelowTen_isNotGraduate() {
      UUID promotionId = UUID.randomUUID();
      UUID groupId = UUID.randomUUID();
      UUID courseId = UUID.randomUUID();
      JUser student = student(UUID.randomUUID(), "Jean", "Dupont");
      JCourse course = course(courseId, "Maths", 3);
      stubPromotionLookup(promotionId);
      when(userRepository.findByRoleAndPromotion_Id(Role.STUDENT, promotionId))
          .thenReturn(List.of(student));
      when(groupHistoryRepository.findByStudent_Id(student.getId()))
          .thenReturn(List.of(history(student.getId(), groupId)));
      when(courseAssignementRepository.findByGroup_IdIn(List.of(groupId)))
          .thenReturn(List.of(assignment(course, groupId)));
      when(examRepository.findByGroups_IdIn(List.of(groupId))).thenReturn(List.of());
      when(gradeRepository.findByStudent_IdAndCourse_Id(student.getId(), courseId))
          .thenReturn(List.of(grade(student.getId(), course, 8f)));

      List<Graduate> graduates = promotionService.getGraduates(promotionId);

      assertThat(graduates).isEmpty();
    }

    @Test
    void coursesComeFromAllGroupsOfTheCursus() {
      UUID promotionId = UUID.randomUUID();
      UUID groupA = UUID.randomUUID();
      UUID groupB = UUID.randomUUID();
      UUID courseA = UUID.randomUUID();
      UUID courseB = UUID.randomUUID();
      JUser student = student(UUID.randomUUID(), "Marie", "Martin");
      JCourse maths = course(courseA, "Maths", 3);
      JCourse physics = course(courseB, "Physique", 2);
      stubPromotionLookup(promotionId);
      when(userRepository.findByRoleAndPromotion_Id(Role.STUDENT, promotionId))
          .thenReturn(List.of(student));
      when(groupHistoryRepository.findByStudent_Id(student.getId()))
          .thenReturn(List.of(history(student.getId(), groupA), history(student.getId(), groupB)));
      when(courseAssignementRepository.findByGroup_IdIn(List.of(groupA, groupB)))
          .thenReturn(List.of(assignment(maths, groupA), assignment(physics, groupB)));
      when(examRepository.findByGroups_IdIn(List.of(groupA, groupB))).thenReturn(List.of());
      when(gradeRepository.findByStudent_IdAndCourse_Id(student.getId(), courseA))
          .thenReturn(List.of(grade(student.getId(), maths, 11f)));
      when(gradeRepository.findByStudent_IdAndCourse_Id(student.getId(), courseB))
          .thenReturn(List.of(grade(student.getId(), physics, 12f)));

      List<Graduate> graduates = promotionService.getGraduates(promotionId);

      assertThat(graduates).hasSize(1);
      assertThat(graduates.get(0).courseAverages())
          .containsEntry("Maths", 11.0f)
          .containsEntry("Physique", 12.0f);
    }

    @Test
    void coursesCanComeFromExamsLinkedToGroups() {
      UUID promotionId = UUID.randomUUID();
      UUID groupId = UUID.randomUUID();
      UUID courseId = UUID.randomUUID();
      JUser student = student(UUID.randomUUID(), "Paul", "Bernard");
      JCourse course = course(courseId, "Algo", 4);
      JExam exam = exam(UUID.randomUUID(), course, 2f);
      stubPromotionLookup(promotionId);
      when(userRepository.findByRoleAndPromotion_Id(Role.STUDENT, promotionId))
          .thenReturn(List.of(student));
      when(groupHistoryRepository.findByStudent_Id(student.getId()))
          .thenReturn(List.of(history(student.getId(), groupId)));
      when(courseAssignementRepository.findByGroup_IdIn(List.of(groupId))).thenReturn(List.of());
      when(examRepository.findByGroups_IdIn(List.of(groupId))).thenReturn(List.of(exam));
      when(gradeRepository.findByStudent_IdAndCourse_Id(student.getId(), courseId))
          .thenReturn(List.of(grade(student.getId(), course, 14f, exam)));

      List<Graduate> graduates = promotionService.getGraduates(promotionId);

      assertThat(graduates).hasSize(1);
      assertThat(graduates.get(0).courseAverages()).containsEntry("Algo", 14.0f);
    }

    @Test
    void courseAverageIsWeightedByExamCoefficient() {
      UUID promotionId = UUID.randomUUID();
      UUID groupId = UUID.randomUUID();
      UUID courseId = UUID.randomUUID();
      JUser student = student(UUID.randomUUID(), "Claire", "Robert");
      JCourse course = course(courseId, "Physique", 2);
      JExam firstExam = exam(UUID.randomUUID(), course, 2f);
      JExam secondExam = exam(UUID.randomUUID(), course, 3f);
      stubPromotionLookup(promotionId);
      when(userRepository.findByRoleAndPromotion_Id(Role.STUDENT, promotionId))
          .thenReturn(List.of(student));
      when(groupHistoryRepository.findByStudent_Id(student.getId()))
          .thenReturn(List.of(history(student.getId(), groupId)));
      when(courseAssignementRepository.findByGroup_IdIn(List.of(groupId)))
          .thenReturn(List.of(assignment(course, groupId)));
      when(examRepository.findByGroups_IdIn(List.of(groupId))).thenReturn(List.of());
      when(gradeRepository.findByStudent_IdAndCourse_Id(student.getId(), courseId))
          .thenReturn(
              List.of(
                  grade(student.getId(), course, 10f, firstExam),
                  grade(student.getId(), course, 12f, secondExam)));

      List<Graduate> graduates = promotionService.getGraduates(promotionId);

      assertThat(graduates).hasSize(1);
      assertThat(graduates.get(0).courseAverages())
          .containsEntry("Physique", (10f * 2f + 12f * 3f) / 5f);
    }

    @Test
    void studentWithNoCourses_isNotGraduate() {
      UUID promotionId = UUID.randomUUID();
      JUser student = student(UUID.randomUUID(), "Solo", "Etudiant");
      stubPromotionLookup(promotionId);
      when(userRepository.findByRoleAndPromotion_Id(Role.STUDENT, promotionId))
          .thenReturn(List.of(student));
      when(groupHistoryRepository.findByStudent_Id(student.getId())).thenReturn(List.of());

      List<Graduate> graduates = promotionService.getGraduates(promotionId);

      assertThat(graduates).isEmpty();
    }

    @Test
    void graduatesAreSortedByLastNameThenFirstName() {
      UUID promotionId = UUID.randomUUID();
      UUID groupId = UUID.randomUUID();
      UUID courseId = UUID.randomUUID();
      JUser first = student(UUID.randomUUID(), "Zoe", "Alpha");
      JUser second = student(UUID.randomUUID(), "Anna", "Bravo");
      JUser third = student(UUID.randomUUID(), "Bob", "Alpha");
      JCourse course = course(courseId, "Maths", 3);
      stubPromotionLookup(promotionId);
      when(userRepository.findByRoleAndPromotion_Id(Role.STUDENT, promotionId))
          .thenReturn(List.of(first, second, third));
      for (JUser student : List.of(first, second, third)) {
        when(groupHistoryRepository.findByStudent_Id(student.getId()))
            .thenReturn(List.of(history(student.getId(), groupId)));
        when(courseAssignementRepository.findByGroup_IdIn(List.of(groupId)))
            .thenReturn(List.of(assignment(course, groupId)));
        when(examRepository.findByGroups_IdIn(List.of(groupId))).thenReturn(List.of());
        when(gradeRepository.findByStudent_IdAndCourse_Id(student.getId(), courseId))
            .thenReturn(List.of(grade(student.getId(), course, 12f)));
      }

      List<Graduate> graduates = promotionService.getGraduates(promotionId);

      assertThat(graduates)
          .extracting(Graduate::lastName)
          .containsExactly("Alpha", "Alpha", "Bravo");
      assertThat(graduates).extracting(Graduate::firstName).containsExactly("Bob", "Zoe", "Anna");
    }

    @Test
    void studentWithAssignedCourseButNoGrades_isNotGraduate() {
      UUID promotionId = UUID.randomUUID();
      UUID groupId = UUID.randomUUID();
      UUID courseId = UUID.randomUUID();
      JUser student = student(UUID.randomUUID(), "Sans", "Notes");
      JCourse course = course(courseId, "Maths", 3);
      stubPromotionLookup(promotionId);
      when(userRepository.findByRoleAndPromotion_Id(Role.STUDENT, promotionId))
              .thenReturn(List.of(student));
      when(groupHistoryRepository.findByStudent_Id(student.getId()))
              .thenReturn(List.of(history(student.getId(), groupId)));
      when(courseAssignementRepository.findByGroup_IdIn(List.of(groupId)))
              .thenReturn(List.of(assignment(course, groupId)));
      when(examRepository.findByGroups_IdIn(List.of(groupId))).thenReturn(List.of());
      when(gradeRepository.findByStudent_IdAndCourse_Id(student.getId(), courseId))
              .thenReturn(List.of());

      List<Graduate> graduates = promotionService.getGraduates(promotionId);

      assertThat(graduates).isEmpty();
    }

    private void stubPromotionLookup(UUID promotionId) {
      JPromotion jPromotion = promotion(promotionId);
      when(promotionRepository.findById(promotionId)).thenReturn(Optional.of(jPromotion));
      when(promotionMapper.toDomain(jPromotion))
          .thenReturn(Promotion.builder().id(promotionId).build());
    }
  }

  private static JPromotion promotion(UUID id) {
    return new JPromotion(id, "P2025", "Promo 2025", LocalDateTime.now(), null);
  }

  private static JUser student(UUID id, String first, String last) {
    return new JUser(id, first, last, Role.STUDENT, id + "@h.s", "enc", null);
  }

  private static JGroup group(UUID id) {
    return new JGroup(id, "G");
  }

  private static JGroupHistory history(UUID studentId, UUID groupId) {
    return new JGroupHistory(
        UUID.randomUUID(), group(groupId), student(studentId, "A", "B"), LocalDateTime.now(), null);
  }

  private static JCourse course(UUID id, String title, int credit) {
    return new JCourse(id, "REF", title, credit);
  }

  private static JCourseAssignement assignment(JCourse course, UUID groupId) {
    return new JCourseAssignement(
        UUID.randomUUID(),
        course,
        new JUser(UUID.randomUUID(), "T", "T", Role.TEACHER, "t@h.s", "enc", null),
        group(groupId));
  }

  private static JGrade grade(UUID studentId, JCourse course, Float value) {
    return grade(studentId, course, value, null);
  }

  private static JGrade grade(UUID studentId, JCourse course, Float value, JExam exam) {
    return new JGrade(
        UUID.randomUUID(),
        value,
        student(studentId, "A", "B"),
        new JUser(UUID.randomUUID(), "T", "T", Role.TEACHER, "t@h.s", "enc", null),
        LocalDateTime.now(),
        course,
        exam);
  }

  private static JExam exam(UUID id, JCourse course, Float coefficient) {
    return new JExam(
        id, LocalDateTime.now(), coefficient, course, List.of(group(UUID.randomUUID())));
  }
}
