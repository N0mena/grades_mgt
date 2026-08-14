package hei.school.minou.repository;

import hei.school.minou.repository.model.JPromotion;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotionRepository extends JpaRepository<JPromotion, UUID> {}
