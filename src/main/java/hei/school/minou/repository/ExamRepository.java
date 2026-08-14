package hei.school.minou.repository;

import hei.school.minou.repository.model.JExam;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamRepository extends JpaRepository<JExam, UUID> {

  List<JExam> findByCourse_Id(UUID courseId);

  List<JExam> findByGroups_IdIn(List<UUID> groupIds);
}
