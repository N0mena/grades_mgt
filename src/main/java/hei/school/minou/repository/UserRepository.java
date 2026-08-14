package hei.school.minou.repository;

import hei.school.minou.entity.enums.Role;
import hei.school.minou.repository.model.JUser;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<JUser, UUID> {

  Optional<JUser> findByEmail(String email);

  List<JUser> findByRole(Role role);

  List<JUser> findByRoleAndPromotion_Id(Role role, UUID promotionId);
}
