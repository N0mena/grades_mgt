package hei.school.minou.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.school.minou.entity.GradeHistory;
import hei.school.minou.entity.User;
import hei.school.minou.entity.enums.Role;
import hei.school.minou.exception.ResourceNotFoundException;
import hei.school.minou.mapper.GradeHistoryMapper;
import hei.school.minou.repository.GradeHistoryRepository;
import hei.school.minou.repository.GradeRepository;
import hei.school.minou.repository.model.JCourse;
import hei.school.minou.repository.model.JGrade;
import hei.school.minou.security.SecurityUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class GradeHistoryServiceTest {

  private GradeHistoryRepository gradeHistoryRepository;
  private GradeRepository gradeRepository;
  private GradeHistoryMapper gradeHistoryMapper;
  private SecurityUtils securityUtils;
  private GradeHistoryService gradeHistoryService;

  @BeforeEach
  void setUp() {
    gradeHistoryRepository = mock(GradeHistoryRepository.class);
    gradeRepository = mock(GradeRepository.class);
    gradeHistoryMapper = mock(GradeHistoryMapper.class);
    securityUtils = mock(SecurityUtils.class);
    gradeHistoryService =
        new GradeHistoryService(
            gradeHistoryRepository, gradeRepository, gradeHistoryMapper, securityUtils);
  }

  @Nested
  class GetHistoryByGrade {

    @Test
    void unknownGrade_throwsNotFound() {
      UUID gradeId = UUID.randomUUID();
      when(gradeRepository.findById(gradeId)).thenReturn(Optional.empty());

      assertThatThrownBy(
              () ->
                  gradeHistoryService.getHistoryByGrade(
                      gradeId, User.builder().id(UUID.randomUUID()).role(Role.ADMIN).build()))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessage("Grade not found: " + gradeId);
    }

    @Test
    void knownGrade_checksAccessAndReturnsHistory() {
      UUID gradeId = UUID.randomUUID();
      JGrade jGrade =
          new JGrade(gradeId, 10.0f, null, null, LocalDateTime.now(), new JCourse(), null);
      when(gradeRepository.findById(gradeId)).thenReturn(Optional.of(jGrade));
      User viewer = User.builder().id(UUID.randomUUID()).role(Role.ADMIN).build();
      GradeHistory history = GradeHistory.builder().id(UUID.randomUUID()).oldValue(10.0f).build();
      when(gradeHistoryRepository.findByGrade_Id(gradeId)).thenReturn(List.of());
      when(gradeHistoryMapper.toDomain(null)).thenReturn(null);

      List<GradeHistory> result = gradeHistoryService.getHistoryByGrade(gradeId, viewer);

      assertThat(result).isEmpty();
      verify(securityUtils).assertCanViewGrade(viewer, jGrade);
    }
  }
}
