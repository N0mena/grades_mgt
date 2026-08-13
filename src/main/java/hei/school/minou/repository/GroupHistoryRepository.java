package hei.school.minou.repository;

import hei.school.minou.repository.model.JGradeHistory;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupHistoryRepository extends JpaRepository<JGradeHistory, UUID> {}
