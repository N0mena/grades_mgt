package hei.school.minou.service;

import hei.school.minou.entity.Course;
import hei.school.minou.entity.CourseAssignement;
import hei.school.minou.exception.BadRequestException;
import hei.school.minou.exception.ResourceNotFoundException;
import hei.school.minou.mapper.CourseMapper;
import hei.school.minou.repository.CourseAssignementRepository;
import hei.school.minou.repository.CourseRepository;
import hei.school.minou.repository.GroupRepository;
import hei.school.minou.repository.UserRepository;
import hei.school.minou.repository.model.JCourse;
import hei.school.minou.repository.model.JCourseAssignement;
import hei.school.minou.repository.model.JGroup;
import hei.school.minou.repository.model.JUser;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class CourseService {

  private final CourseRepository courseRepository;
  private final CourseAssignementRepository courseAssignementRepository;
  private final UserRepository userRepository;
  private final GroupRepository groupRepository;
  private final CourseMapper courseMapper;

  public Course saveCourse(Course course) {
    return courseMapper.toDomain(courseRepository.save(courseMapper.toJpa(course)));
  }

  public Course getCourseById(UUID courseId) {
    JCourse jCourse =
        courseRepository
            .findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseId));
    return courseMapper.toDomain(jCourse);
  }

  public List<Course> getAllCourses() {
    return courseRepository.findAll().stream().map(courseMapper::toDomain).toList();
  }

  @Transactional
  public CourseAssignement assignCourse(UUID courseId, UUID teacherId, UUID groupId) {
    JCourse course =
        courseRepository
            .findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseId));
    JUser teacher =
        userRepository
            .findById(teacherId)
            .orElseThrow(() -> new ResourceNotFoundException("Teacher not found: " + teacherId));
    JGroup group =
        groupRepository
            .findById(groupId)
            .orElseThrow(() -> new ResourceNotFoundException("Group not found: " + groupId));

    if (courseAssignementRepository.existsByCourse_IdAndTeacher_IdAndGroup_Id(
        courseId, teacherId, groupId)) {
      throw new BadRequestException("Course already assigned to this teacher and group");
    }

    JCourseAssignement jAssignment =
        new JCourseAssignement(UUID.randomUUID(), course, teacher, group);
    return courseMapper.toDomain(courseAssignementRepository.save(jAssignment));
  }

  public List<CourseAssignement> getAssignmentsByCourse(UUID courseId) {
    return courseAssignementRepository.findByCourse_Id(courseId).stream()
        .map(courseMapper::toDomain)
        .toList();
  }

  public List<CourseAssignement> getAssignmentsByTeacher(UUID teacherId) {
    return courseAssignementRepository.findByTeacher_Id(teacherId).stream()
        .map(courseMapper::toDomain)
        .toList();
  }

  public List<CourseAssignement> getAssignmentsByGroup(UUID groupId) {
    return courseAssignementRepository.findByGroup_Id(groupId).stream()
        .map(courseMapper::toDomain)
        .toList();
  }
}
