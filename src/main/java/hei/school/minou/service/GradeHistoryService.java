package hei.school.minou.service;

import hei.school.minou.entity.GradeHistory;
import hei.school.minou.entity.User;
import hei.school.minou.mapper.GradeHistoryMapper;
import hei.school.minou.repository.GradeHistoryRepository;
import hei.school.minou.repository.GradeRepository;
import hei.school.minou.repository.model.JGrade;
import hei.school.minou.security.SecurityUtils;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GradeHistoryService {

  private final GradeHistoryRepository gradeHistoryRepository;
  private final GradeRepository gradeRepository;
  private final GradeHistoryMapper gradeHistoryMapper;
  private final SecurityUtils accessControlService;

  public List<GradeHistory> getHistoryByGrade(UUID gradeId, User viewer) {
    JGrade jGrade =
        gradeRepository
            .findById(gradeId)
            .orElseThrow(() -> new RuntimeException("Grade not found: " + gradeId));
    accessControlService.assertCanViewGrade(viewer, jGrade);
    return gradeHistoryRepository.findByGrade_Id(gradeId).stream()
        .map(gradeHistoryMapper::toDomain)
        .toList();
  }
}
