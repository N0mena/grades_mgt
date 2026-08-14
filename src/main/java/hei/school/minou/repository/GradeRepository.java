package hei.school.minou.repository;

import hei.school.minou.repository.model.JGrade;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GradeRepository extends JpaRepository<JGrade, UUID> {

  List<JGrade> findByStudent_Id(UUID studentId);

  List<JGrade> findByCourse_Id(UUID courseId);
}
