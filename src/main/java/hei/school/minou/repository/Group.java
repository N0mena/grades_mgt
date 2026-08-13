package hei.school.minou.repository;

import hei.school.minou.repository.model.JGroup;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Group extends JpaRepository<JGroup, UUID> {}
