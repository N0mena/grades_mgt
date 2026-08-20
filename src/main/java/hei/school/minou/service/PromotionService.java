package hei.school.minou.service;

import hei.school.minou.endpoint.rest.controller.dto.Graduate;
import hei.school.minou.endpoint.rest.controller.dto.PromotionResults;
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
import hei.school.minou.repository.model.JPromotion;
import hei.school.minou.repository.model.JUser;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class PromotionService {

  private static final float PASSING_GRADE = 10.0f;

  private final PromotionRepository promotionRepository;
  private final UserRepository userRepository;
  private final GroupHistoryRepository groupHistoryRepository;
  private final GradeRepository gradeRepository;
  private final CourseAssignementRepository courseAssignementRepository;
  private final ExamRepository examRepository;
  private final PromotionMapper promotionMapper;
  private final UserMapper userMapper;

  public Promotion savePromotion(Promotion promotion) {
    Promotion toSave =
        promotion.id() != null
            ? promotion
            : Promotion.builder()
                .id(UUID.randomUUID())
                .ref(promotion.ref())
                .name(promotion.name())
                .startDate(promotion.startDate())
                .endDate(promotion.endDate())
                .build();
    return promotionMapper.toDomain(promotionRepository.save(promotionMapper.toJpa(toSave)));
  }

  public Promotion getPromotionById(UUID id) {
    JPromotion jPromotion =
        promotionRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Promotion not found: " + id));
    return promotionMapper.toDomain(jPromotion);
  }

  public List<Promotion> getAllPromotions() {
    return promotionRepository.findAll().stream().map(promotionMapper::toDomain).toList();
  }

  @Transactional
  public User assignStudent(UUID promotionId, UUID studentId) {
    JPromotion promotion =
        promotionRepository
            .findById(promotionId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Promotion not found: " + promotionId));
    JUser student =
        userRepository
            .findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));
    student.setPromotion(promotion);
    return userMapper.toDomain(userRepository.save(student));
  }

  public List<Graduate> getGraduates(UUID promotionId) {
    getPromotionById(promotionId);
    return userRepository.findByRoleAndPromotion_Id(Role.STUDENT, promotionId).stream()
        .map(student -> toGraduate(student, computeGraduation(student)))
        .filter(Objects::nonNull)
        .sorted(Comparator.comparing(Graduate::lastName).thenComparing(Graduate::firstName))
        .toList();
  }

  public PromotionResults getResults(UUID promotionId) {
    Promotion promotion = getPromotionById(promotionId);
    List<PromotionResults.StudentYearResult> students =
        userRepository.findByRoleAndPromotion_Id(Role.STUDENT, promotionId).stream()
            .map(student -> toStudentYearResult(student, promotion))
            .sorted(
                Comparator.comparing(PromotionResults.StudentYearResult::lastName)
                    .thenComparing(PromotionResults.StudentYearResult::firstName))
            .toList();
    return PromotionResults.builder()
        .promotionId(promotion.id())
        .promotionRef(promotion.ref())
        .promotionName(promotion.name())
        .students(students)
        .build();
  }

  private PromotionResults.StudentYearResult toStudentYearResult(
      JUser student, Promotion promotion) {
    List<JGrade> grades = gradeRepository.findByStudent_Id(student.getId());
    Map<String, Float> courseAverages = courseAveragesFromGrades(grades);
    Float overall = overallFromCourseAverages(student.getId(), courseAverages);
    List<PromotionResults.YearResult> years = yearResults(grades, promotion.startDate());
    return PromotionResults.StudentYearResult.builder()
        .studentId(student.getId())
        .firstName(student.getFirstName())
        .lastName(student.getLastName())
        .email(student.getEmail())
        .overallAverage(overall)
        .courseAverages(courseAverages)
        .years(years)
        .build();
  }

  private List<PromotionResults.YearResult> yearResults(
      List<JGrade> grades, LocalDateTime promotionStart) {
    LocalDateTime start =
        promotionStart != null
            ? promotionStart
            : grades.stream()
                .map(JGrade::getCreatedAt)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
    List<PromotionResults.YearResult> years = new ArrayList<>();
    for (int year = 1; year <= 3; year++) {
      LocalDateTime yearStart = start.plusYears(year - 1L);
      LocalDateTime yearEnd = start.plusYears(year);
      List<JGrade> yearGrades =
          grades.stream()
              .filter(grade -> grade.getCreatedAt() != null)
              .filter(
                  grade ->
                      !grade.getCreatedAt().isBefore(yearStart)
                          && grade.getCreatedAt().isBefore(yearEnd))
              .toList();
      Map<String, Float> averages = courseAveragesFromGrades(yearGrades);
      Float average =
          averages.isEmpty()
              ? null
              : (float)
                  averages.values().stream().mapToDouble(Float::doubleValue).average().orElse(0);
      years.add(
          PromotionResults.YearResult.builder()
              .year(year)
              .average(average)
              .courseAverages(averages)
              .build());
    }
    return years;
  }

  private Map<String, Float> courseAveragesFromGrades(List<JGrade> grades) {
    Map<UUID, List<JGrade>> byCourse = new LinkedHashMap<>();
    Map<UUID, JCourse> courses = new LinkedHashMap<>();
    for (JGrade grade : grades) {
      if (grade.getCourse() == null) {
        continue;
      }
      byCourse.computeIfAbsent(grade.getCourse().getId(), unused -> new ArrayList<>()).add(grade);
      courses.putIfAbsent(grade.getCourse().getId(), grade.getCourse());
    }
    Map<String, Float> averages = new LinkedHashMap<>();
    for (Map.Entry<UUID, List<JGrade>> entry : byCourse.entrySet()) {
      averages.put(courseTitle(courses.get(entry.getKey())), averageOfGrades(entry.getValue()));
    }
    return averages;
  }

  private Float overallFromCourseAverages(UUID studentId, Map<String, Float> courseAverages) {
    if (courseAverages.isEmpty()) {
      return null;
    }
    List<JCourse> courses = coursesOfCursus(studentId);
    if (courses.isEmpty()) {
      return (float)
          courseAverages.values().stream().mapToDouble(Float::doubleValue).average().orElse(0);
    }
    float weightedSum = 0;
    int totalCredit = 0;
    for (JCourse course : courses) {
      Float average = courseAverages.get(courseTitle(course));
      if (average == null) {
        continue;
      }
      int credit = course.getCredit() != null ? course.getCredit() : 1;
      weightedSum += average * credit;
      totalCredit += credit;
    }
    return totalCredit == 0 ? null : weightedSum / totalCredit;
  }

  private float averageOfGrades(List<JGrade> grades) {
    float weightedSum = 0;
    float totalCoefficient = 0;
    for (JGrade grade : grades) {
      float value = grade.getValue() != null ? grade.getValue() : 0f;
      float coefficient = coefficientOf(grade);
      weightedSum += value * coefficient;
      totalCoefficient += coefficient;
    }
    return totalCoefficient == 0 ? 0 : weightedSum / totalCoefficient;
  }

  private Graduation computeGraduation(JUser student) {
    List<JCourse> courses = coursesOfCursus(student.getId());
    if (courses.isEmpty()) {
      return null;
    }
    Map<String, Float> courseAverages = new LinkedHashMap<>();
    float overallWeightedSum = 0;
    int totalCredit = 0;
    for (JCourse course : courses) {
      float average = courseAverage(student.getId(), course.getId());
      courseAverages.put(courseTitle(course), average);
      int credit = course.getCredit() != null ? course.getCredit() : 1;
      overallWeightedSum += average * credit;
      totalCredit += credit;
    }
    if (courseAverages.values().stream().anyMatch(average -> average < PASSING_GRADE)) {
      return null;
    }
    float overallAverage = totalCredit == 0 ? 0 : overallWeightedSum / totalCredit;
    return new Graduation(courseAverages, overallAverage);
  }

  private List<UUID> groupsOfCursus(UUID studentId) {
    return groupHistoryRepository.findByStudent_Id(studentId).stream()
        .map(history -> history.getGroup().getId())
        .distinct()
        .toList();
  }

  private List<JCourse> coursesOfCursus(UUID studentId) {
    List<UUID> groupIds = groupsOfCursus(studentId);
    if (groupIds.isEmpty()) {
      return List.of();
    }
    Map<UUID, JCourse> courses = new LinkedHashMap<>();
    courseAssignementRepository.findByGroup_IdIn(groupIds).stream()
        .map(JCourseAssignement::getCourse)
        .forEach(course -> courses.put(course.getId(), course));
    examRepository.findByGroups_IdIn(groupIds).stream()
        .map(JExam::getCourse)
        .filter(Objects::nonNull)
        .forEach(course -> courses.put(course.getId(), course));
    return new ArrayList<>(courses.values());
  }

  private float courseAverage(UUID studentId, UUID courseId) {
    List<JGrade> grades = gradeRepository.findByStudent_IdAndCourse_Id(studentId, courseId);
    float weightedSum = 0;
    float totalCoefficient = 0;
    for (JGrade grade : grades) {
      float value = grade.getValue() != null ? grade.getValue() : 0f;
      float coefficient = coefficientOf(grade);
      weightedSum += value * coefficient;
      totalCoefficient += coefficient;
    }
    return totalCoefficient == 0 ? 0 : weightedSum / totalCoefficient;
  }

  private float coefficientOf(JGrade grade) {
    JExam exam = grade.getExam();
    Float coefficient = exam != null ? exam.getCoefficient() : null;
    return coefficient != null ? coefficient : 1f;
  }

  private String courseTitle(JCourse course) {
    return course.getTitle() != null ? course.getTitle() : course.getRef();
  }

  private Graduate toGraduate(JUser student, Graduation graduation) {
    if (graduation == null) {
      return null;
    }
    return Graduate.builder()
        .id(student.getId())
        .firstName(student.getFirstName())
        .lastName(student.getLastName())
        .email(student.getEmail())
        .overallAverage(graduation.overallAverage())
        .courseAverages(graduation.courseAverages())
        .build();
  }

  private record Graduation(Map<String, Float> courseAverages, float overallAverage) {}
}
