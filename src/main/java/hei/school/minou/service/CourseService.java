package hei.school.minou.service;

import hei.school.minou.entity.Course;
import hei.school.minou.entity.CourseAssignement;
import hei.school.minou.entity.Group;
import hei.school.minou.entity.User;
import hei.school.minou.entity.enums.Role;
import hei.school.minou.exception.BadRequestException;
import hei.school.minou.exception.ResourceNotFoundException;
import hei.school.minou.mapper.CourseMapper;
import hei.school.minou.mapper.GroupMapper;
import hei.school.minou.mapper.UserMapper;
import hei.school.minou.repository.CourseAssignementRepository;
import hei.school.minou.repository.CourseRepository;
import hei.school.minou.repository.GroupHistoryRepository;
import hei.school.minou.repository.GroupRepository;
import hei.school.minou.repository.UserRepository;
import hei.school.minou.repository.model.JCourse;
import hei.school.minou.repository.model.JCourseAssignement;
import hei.school.minou.repository.model.JGroup;
import hei.school.minou.repository.model.JUser;
import hei.school.minou.security.SecurityUtils;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
  private final GroupHistoryRepository groupHistoryRepository;
  private final CourseMapper courseMapper;
  private final GroupMapper groupMapper;
  private final UserMapper userMapper;
  private final SecurityUtils securityUtils;

  public Course saveCourse(Course course) {
    Course toSave =
        course.id() != null
            ? course
            : Course.builder()
                .id(UUID.randomUUID())
                .ref(course.ref())
                .title(course.title())
                .credit(course.credit())
                .build();
    return courseMapper.toDomain(courseRepository.save(courseMapper.toJpa(toSave)));
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

  public List<Course> getCoursesForTeacher(User viewer) {
    securityUtils.assertTeacherOrAdmin(viewer);
    UUID teacherId = viewer.role() == Role.ADMIN ? null : viewer.id();
    if (teacherId == null) {
      return getAllCourses();
    }
    Map<UUID, Course> courses = new LinkedHashMap<>();
    courseAssignementRepository.findByTeacher_Id(teacherId).stream()
        .map(assignment -> courseMapper.toDomain(assignment.getCourse()))
        .forEach(course -> courses.put(course.id(), course));
    return new ArrayList<>(courses.values());
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

  @Transactional
  public List<CourseAssignement> setTeachers(UUID courseId, List<UUID> teacherIds) {
    JCourse course = requireCourse(courseId);
    List<JGroup> groups =
        courseAssignementRepository.findByCourse_Id(courseId).stream()
            .map(JCourseAssignement::getGroup)
            .distinct()
            .toList();
    List<JUser> teachers = resolveTeachers(teacherIds);
    courseAssignementRepository.deleteByCourse_Id(courseId);
    return saveAssignments(course, teachers, groups);
  }

  @Transactional
  public List<CourseAssignement> setGroups(UUID courseId, List<UUID> groupIds) {
    JCourse course = requireCourse(courseId);
    List<JUser> teachers =
        courseAssignementRepository.findByCourse_Id(courseId).stream()
            .map(JCourseAssignement::getTeacher)
            .distinct()
            .toList();
    List<JGroup> groups = resolveGroups(groupIds);
    courseAssignementRepository.deleteByCourse_Id(courseId);
    return saveAssignments(course, teachers, groups);
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

  public List<Group> getGroupsForCourse(UUID courseId, User viewer) {
    securityUtils.assertCanAccessCourse(viewer, courseId);
    Map<UUID, Group> groups = new LinkedHashMap<>();
    courseAssignementRepository.findByCourse_Id(courseId).stream()
        .filter(
            assignment ->
                viewer.role() == Role.ADMIN
                    || assignment.getTeacher().getId().equals(viewer.id()))
        .map(assignment -> groupMapper.toDomain(assignment.getGroup()))
        .forEach(group -> groups.put(group.id(), group));
    return new ArrayList<>(groups.values());
  }

  public List<User> getStudentsForCourse(UUID courseId, User viewer) {
    securityUtils.assertCanAccessCourse(viewer, courseId);
    Set<UUID> groupIds = new LinkedHashSet<>();
    courseAssignementRepository.findByCourse_Id(courseId).stream()
        .filter(
            assignment ->
                viewer.role() == Role.ADMIN
                    || assignment.getTeacher().getId().equals(viewer.id()))
        .map(assignment -> assignment.getGroup().getId())
        .forEach(groupIds::add);
    Map<UUID, User> students = new LinkedHashMap<>();
    for (UUID groupId : groupIds) {
      groupHistoryRepository.findByGroup_IdAndEndDateIsNull(groupId).stream()
          .map(history -> userMapper.toDomain(history.getStudent()))
          .forEach(student -> students.put(student.id(), student));
    }
    return new ArrayList<>(students.values());
  }

  private JCourse requireCourse(UUID courseId) {
    return courseRepository
        .findById(courseId)
        .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseId));
  }

  private List<JUser> resolveTeachers(List<UUID> teacherIds) {
    if (teacherIds == null || teacherIds.isEmpty()) {
      return List.of();
    }
    List<JUser> teachers = new ArrayList<>();
    for (UUID teacherId : teacherIds) {
      JUser teacher =
          userRepository
              .findById(teacherId)
              .orElseThrow(() -> new ResourceNotFoundException("Teacher not found: " + teacherId));
      if (teacher.getRole() != Role.TEACHER && teacher.getRole() != Role.ADMIN) {
        throw new BadRequestException("User is not a teacher: " + teacherId);
      }
      teachers.add(teacher);
    }
    return teachers;
  }

  private List<JGroup> resolveGroups(List<UUID> groupIds) {
    if (groupIds == null || groupIds.isEmpty()) {
      return List.of();
    }
    List<JGroup> groups = new ArrayList<>();
    for (UUID groupId : groupIds) {
      groups.add(
          groupRepository
              .findById(groupId)
              .orElseThrow(() -> new ResourceNotFoundException("Group not found: " + groupId)));
    }
    return groups;
  }

  private List<CourseAssignement> saveAssignments(
      JCourse course, List<JUser> teachers, List<JGroup> groups) {
    if (teachers.isEmpty() || groups.isEmpty()) {
      return List.of();
    }
    List<CourseAssignement> saved = new ArrayList<>();
    for (JUser teacher : teachers) {
      for (JGroup group : groups) {
        JCourseAssignement assignment =
            new JCourseAssignement(UUID.randomUUID(), course, teacher, group);
        saved.add(courseMapper.toDomain(courseAssignementRepository.save(assignment)));
      }
    }
    return saved;
  }
}
