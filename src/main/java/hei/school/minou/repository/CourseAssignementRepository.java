package hei.school.minou.repository;

import hei.school.minou.repository.model.JCourseAssignement;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseAssignementRepository extends JpaRepository<JCourseAssignement, UUID> {

  List<JCourseAssignement> findByCourse_Id(UUID courseId);

  List<JCourseAssignement> findByTeacher_Id(UUID teacherId);

  List<JCourseAssignement> findByGroup_Id(UUID groupId);

  boolean existsByCourse_IdAndTeacher_IdAndGroup_Id(UUID courseId, UUID teacherId, UUID groupId);
}
