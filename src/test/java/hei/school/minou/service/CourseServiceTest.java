package hei.school.minou.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.school.minou.entity.Course;
import hei.school.minou.entity.CourseAssignement;
import hei.school.minou.exception.BadRequestException;
import hei.school.minou.exception.ResourceNotFoundException;
import hei.school.minou.mapper.CourseMapper;
import hei.school.minou.mapper.GroupMapper;
import hei.school.minou.mapper.UserMapper;
import hei.school.minou.repository.*;
import hei.school.minou.repository.model.JCourse;
import hei.school.minou.repository.model.JCourseAssignement;
import hei.school.minou.repository.model.JGroup;
import hei.school.minou.repository.model.JUser;
import hei.school.minou.security.SecurityUtils;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CourseServiceTest {

  private CourseRepository courseRepository;
  private CourseAssignementRepository courseAssignementRepository;
  private UserRepository userRepository;
  private GroupRepository groupRepository;
  private GroupHistoryRepository groupHistoryRepository;
  private CourseMapper courseMapper;
  private GroupMapper groupMapper;
  private UserMapper userMapper;
  private SecurityUtils securityUtils;
  private CourseService courseService;

  @BeforeEach
  void setUp() {
    courseRepository = mock(CourseRepository.class);
    courseAssignementRepository = mock(CourseAssignementRepository.class);
    userRepository = mock(UserRepository.class);
    groupRepository = mock(GroupRepository.class);
    GroupHistoryRepository groupHistoryRepository = mock(GroupHistoryRepository.class);
    courseMapper = mock(CourseMapper.class);
    GroupMapper groupMapper = mock(GroupMapper.class);
    UserMapper userMapper = mock(UserMapper.class);
    SecurityUtils securityUtils = mock(SecurityUtils.class);
    courseService =
        new CourseService(
            courseRepository,
            courseAssignementRepository,
            userRepository,
            groupRepository,
            groupHistoryRepository,
            courseMapper,
            groupMapper,
            userMapper,
            securityUtils);
  }

  @Nested
  class GetCourseById {

    @Test
    void unknownCourse_throwsNotFound() {
      UUID id = UUID.randomUUID();
      when(courseRepository.findById(id)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> courseService.getCourseById(id))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessage("Course not found: " + id);
    }

    @Test
    void knownCourse_isReturned() {
      JCourse jCourse = new JCourse(UUID.randomUUID(), "PROG4", "Prog", 4);
      Course expected = Course.builder().id(jCourse.getId()).title("Prog").build();
      when(courseRepository.findById(jCourse.getId())).thenReturn(Optional.of(jCourse));
      when(courseMapper.toDomain(jCourse)).thenReturn(expected);

      assertThat(courseService.getCourseById(jCourse.getId())).isEqualTo(expected);
    }
  }

  @Nested
  class AssignCourse {

    @Test
    void unknownCourse_throwsNotFound() {
      UUID courseId = UUID.randomUUID();
      when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

      assertThatThrownBy(
              () -> courseService.assignCourse(courseId, UUID.randomUUID(), UUID.randomUUID()))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessage("Course not found: " + courseId);
    }

    @Test
    void unknownTeacher_throwsNotFound() {
      UUID courseId = UUID.randomUUID();
      UUID teacherId = UUID.randomUUID();
      when(courseRepository.findById(courseId))
          .thenReturn(Optional.of(new JCourse(courseId, "P", "T", 1)));
      when(userRepository.findById(teacherId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> courseService.assignCourse(courseId, teacherId, UUID.randomUUID()))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessage("Teacher not found: " + teacherId);
    }

    @Test
    void duplicateAssignment_throwsBadRequest() {
      UUID courseId = UUID.randomUUID();
      UUID teacherId = UUID.randomUUID();
      UUID groupId = UUID.randomUUID();
      when(courseRepository.findById(courseId))
          .thenReturn(Optional.of(new JCourse(courseId, "P", "T", 1)));
      when(userRepository.findById(teacherId)).thenReturn(Optional.of(jUser(teacherId)));
      when(groupRepository.findById(groupId)).thenReturn(Optional.of(new JGroup(groupId, "G")));
      when(courseAssignementRepository.existsByCourse_IdAndTeacher_IdAndGroup_Id(
              courseId, teacherId, groupId))
          .thenReturn(true);

      assertThatThrownBy(() -> courseService.assignCourse(courseId, teacherId, groupId))
          .isInstanceOf(BadRequestException.class)
          .hasMessage("Course already assigned to this teacher and group");
      verify(courseAssignementRepository, never()).save(any());
    }

    @Test
    void validAssignment_isSaved() {
      UUID courseId = UUID.randomUUID();
      UUID teacherId = UUID.randomUUID();
      UUID groupId = UUID.randomUUID();
      JCourse course = new JCourse(courseId, "P", "T", 1);
      JUser teacher = jUser(teacherId);
      JGroup group = new JGroup(groupId, "G");
      when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
      when(userRepository.findById(teacherId)).thenReturn(Optional.of(teacher));
      when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
      when(courseAssignementRepository.existsByCourse_IdAndTeacher_IdAndGroup_Id(
              courseId, teacherId, groupId))
          .thenReturn(false);
      JCourseAssignement saved = new JCourseAssignement(UUID.randomUUID(), course, teacher, group);
      when(courseAssignementRepository.save(any(JCourseAssignement.class))).thenReturn(saved);
      when(courseMapper.toDomain(saved))
          .thenReturn(CourseAssignement.builder().id(saved.getId()).build());

      CourseAssignement result = courseService.assignCourse(courseId, teacherId, groupId);

      assertThat(result.id()).isEqualTo(saved.getId());
      verify(courseAssignementRepository).save(any(JCourseAssignement.class));
    }
  }

  @Nested
  class GetAssignments {

    @Test
    void byCourse_returnsList() {
      UUID courseId = UUID.randomUUID();
      JCourseAssignement j = new JCourseAssignement();
      when(courseAssignementRepository.findByCourse_Id(courseId)).thenReturn(List.of(j));
      when(courseMapper.toDomain(j))
          .thenReturn(CourseAssignement.builder().id(UUID.randomUUID()).build());

      assertThat(courseService.getAssignmentsByCourse(courseId)).hasSize(1);
    }
  }

  private static JUser jUser(UUID id) {
    return new JUser(
        id, "A", "M", hei.school.minou.entity.enums.Role.TEACHER, "t@h.s", "enc", null);
  }
}
