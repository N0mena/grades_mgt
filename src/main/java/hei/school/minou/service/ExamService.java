package hei.school.minou.service;

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
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ExamService {

  private final ExamRepository examRepository;
  private final CourseRepository courseRepository;
  private final GroupRepository groupRepository;
  private final ExamMapper examMapper;

  @Transactional
  public Exam createExam(
      UUID courseId, LocalDateTime examDate, Float coefficient, List<UUID> groupIds) {
    JCourse course =
        courseRepository
            .findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseId));
    JExam jExam = new JExam(UUID.randomUUID(), examDate, coefficient, course, null);
    jExam.setGroups(loadGroups(groupIds));
    return examMapper.toDomain(examRepository.save(jExam));
  }

  public Exam getExamById(UUID id) {
    JExam jExam =
        examRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Exam not found: " + id));
    return examMapper.toDomain(jExam);
  }

  public List<Exam> getAllExams() {
    return examRepository.findAll().stream().map(examMapper::toDomain).toList();
  }

  public List<Exam> getExamsByCourse(UUID courseId) {
    return examRepository.findByCourse_Id(courseId).stream().map(examMapper::toDomain).toList();
  }

  @Transactional
  public void linkGroups(UUID examId, List<UUID> groupIds) {
    JExam jExam =
        examRepository
            .findById(examId)
            .orElseThrow(() -> new ResourceNotFoundException("Exam not found: " + examId));
    jExam.setGroups(loadGroups(groupIds));
    examRepository.save(jExam);
  }

  private List<JGroup> loadGroups(List<UUID> groupIds) {
    if (groupIds == null) {
      return null;
    }
    return groupIds.stream()
        .map(
            id ->
                groupRepository
                    .findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Group not found: " + id)))
        .toList();
  }
}
