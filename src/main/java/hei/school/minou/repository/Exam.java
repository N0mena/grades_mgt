package hei.school.minou.repository;

import hei.school.minou.repository.model.JExam;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Exam extends JpaRepository<JExam, UUID> {}
