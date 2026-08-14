package hei.school.minou.endpoint.rest.controller.controller;

import static org.assertj.core.api.Assertions.assertThat;

import hei.school.minou.conf.FacadeIT;
import hei.school.minou.endpoint.rest.controller.dto.CourseAssignementRequest;
import hei.school.minou.endpoint.rest.controller.dto.GradeRequest;
import hei.school.minou.endpoint.rest.controller.dto.LoginRequest;
import hei.school.minou.endpoint.rest.controller.dto.LoginResponse;
import hei.school.minou.endpoint.rest.controller.dto.UpdateGradeRequest;
import hei.school.minou.entity.Course;
import hei.school.minou.entity.Grade;
import hei.school.minou.entity.GradeHistory;
import hei.school.minou.entity.Group;
import hei.school.minou.entity.User;
import hei.school.minou.entity.enums.Role;
import hei.school.minou.repository.UserRepository;
import hei.school.minou.repository.model.JUser;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

class GradeFlowIT extends FacadeIT {

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private UserRepository userRepository;
  @Autowired private PasswordEncoder passwordEncoder;

  private String adminToken;

  @BeforeEach
  void seedAdmin() {
    if (userRepository.findByEmail("admin@hei.school").isEmpty()) {
      userRepository.save(
          new JUser(
              UUID.randomUUID(),
              "Admin",
              "Root",
              Role.ADMIN,
              "admin@hei.school",
              passwordEncoder.encode("admin123")));
    }
    adminToken = login("admin@hei.school", "admin123");
  }

  @Test
  void fullGradeLifecycle_respectsAccessRules() {
    User admin = get("/users/me", adminToken, User.class).getBody();
    assertThat(admin.role()).isEqualTo(Role.ADMIN);

    User teacher = createUser(Role.TEACHER, "teacher@hei.school", "t123");
    User student1 = createUser(Role.STUDENT, "s1@hei.school", "s123");
    User student2 = createUser(Role.STUDENT, "s2@hei.school", "s123");

    Course course =
        post(
                "/courses",
                Map.of(
                    "id",
                    UUID.randomUUID().toString(),
                    "ref",
                    "PROG4",
                    "title",
                    "Prog",
                    "credit",
                    4),
                adminToken,
                Course.class)
            .getBody();
    Group group1 =
        post(
                "/groups",
                Map.of("id", UUID.randomUUID().toString(), "ref", "G1"),
                adminToken,
                Group.class)
            .getBody();
    Group group2 =
        post(
                "/groups",
                Map.of("id", UUID.randomUUID().toString(), "ref", "G2"),
                adminToken,
                Group.class)
            .getBody();

    post(
        "/courses/" + course.id() + "/assignements",
        new CourseAssignementRequest(teacher.id(), group1.id()),
        adminToken,
        Object.class);

    put(
        "/students/" + student1.id() + "/group",
        Map.of("newGroupId", group1.id().toString()),
        adminToken,
        Object.class);
    put(
        "/students/" + student2.id() + "/group",
        Map.of("newGroupId", group2.id().toString()),
        adminToken,
        Object.class);

    String teacherToken = login("teacher@hei.school", "t123");
    String student1Token = login("s1@hei.school", "s123");
    String student2Token = login("s2@hei.school", "s123");

    Grade grade =
        post(
                "/grades",
                new GradeRequest(student1.id(), teacher.id(), course.id(), null, 12.0f),
                teacherToken,
                Grade.class)
            .getBody();

    assertThat(grade).isNotNull();

    assertForbidden(get("/grades/courses/" + course.id(), student1Token, Object.class));
    assertForbidden(get("/grades/students/" + student2.id(), student1Token, Object.class));

    ResponseEntity<List<Grade>> teacherCourseGrades =
        getList("/grades/courses/" + course.id(), teacherToken, Grade.class);
    assertThat(teacherCourseGrades.getBody()).hasSize(1);

    Grade updated =
        put(
                "/grades/" + grade.id(),
                new UpdateGradeRequest(14.0f, "Erreur de saisie corrigee"),
                teacherToken,
                Grade.class)
            .getBody();
    assertThat(updated.value()).isEqualTo(14.0f);

    ResponseEntity<List<GradeHistory>> history =
        getList("/grades/" + grade.id() + "/history", teacherToken, GradeHistory.class);
    assertThat(history.getBody().get(0).oldValue()).isEqualTo(12.0f);
    assertThat(history.getBody().get(0).newValue()).isEqualTo(14.0f);
    assertThat(history.getBody().get(0).reason()).isEqualTo("Erreur de saisie corrigee");

    ResponseEntity<List<Grade>> studentGrades =
        getList("/grades/students/" + student1.id(), student1Token, Grade.class);
    assertThat(studentGrades.getBody()).hasSize(1);

    ResponseEntity<Grade> studentForbiddenUpdate =
        exchange(
            "/grades/" + grade.id(),
            HttpMethod.PUT,
            new UpdateGradeRequest(15.0f, "triche"),
            student1Token,
            Grade.class);
    assertForbidden(studentForbiddenUpdate);

    ResponseEntity<Grade> otherStudentGrades =
        get("/grades/students/" + student1.id(), student2Token, Grade.class);
    assertForbidden(otherStudentGrades);

    ResponseEntity<List<Grade>> allGrades = getList("/grades", adminToken, Grade.class);
    assertThat(allGrades.getBody()).hasSize(1);
  }

  private User createUser(Role role, String email, String password) {
    User body =
        User.builder()
            .id(UUID.randomUUID())
            .firstName("F")
            .lastName("L")
            .role(role)
            .email(email)
            .password(password)
            .build();
    return post("/users", body, adminToken, User.class).getBody();
  }

  private String login(String email, String password) {
    ResponseEntity<LoginResponse> response =
        post("/auth/login", new LoginRequest(email, password), null, LoginResponse.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    return response.getBody().token();
  }

  private <T> ResponseEntity<T> get(String path, String token, Class<T> type) {
    return restTemplate.exchange(path, HttpMethod.GET, entity(null, token), type);
  }

  private <T> ResponseEntity<List<T>> getList(String path, String token, Class<T> elementType) {
    ResponseEntity<List<T>> response =
        restTemplate.exchange(
            path, HttpMethod.GET, entity(null, token), new ParameterizedTypeReference<>() {});
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    return response;
  }

  private <T> ResponseEntity<T> post(String path, Object body, String token, Class<T> type) {
    ResponseEntity<T> response =
        restTemplate.exchange(path, HttpMethod.POST, entity(body, token), type);
    assertSuccess(response, path);
    return response;
  }

  private <T> ResponseEntity<T> put(String path, Object body, String token, Class<T> type) {
    ResponseEntity<T> response =
        restTemplate.exchange(path, HttpMethod.PUT, entity(body, token), type);
    assertSuccess(response, path);
    return response;
  }

  private <T> ResponseEntity<T> exchange(
      String path, HttpMethod method, Object body, String token, Class<T> type) {
    return restTemplate.exchange(path, method, entity(body, token), type);
  }

  private void assertSuccess(ResponseEntity<?> response, String path) {
    assertThat(response.getStatusCode())
        .withFailMessage("Unexpected status %s for %s", response.getStatusCode(), path)
        .isEqualTo(HttpStatus.OK);
  }

  private void assertForbidden(ResponseEntity<?> response) {
    assertThat(response.getStatusCode())
        .withFailMessage("Expected 403 but got %s", response.getStatusCode())
        .isEqualTo(HttpStatus.FORBIDDEN);
  }

  private HttpEntity<Object> entity(Object body, String token) {
    HttpHeaders headers = new HttpHeaders();
    if (token != null) {
      headers.setBearerAuth(token);
    }
    return new HttpEntity<>(body, headers);
  }
}
