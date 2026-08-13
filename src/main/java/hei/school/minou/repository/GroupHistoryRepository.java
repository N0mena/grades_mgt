package hei.school.minou.repository;

import hei.school.minou.repository.model.JGroupHistory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupHistoryRepository extends JpaRepository<JGroupHistory, UUID> {

  List<JGroupHistory> findByStudent_Id(UUID studentId);

  List<JGroupHistory> findByStudent_IdAndEndDateIsNull(UUID studentId);
}
