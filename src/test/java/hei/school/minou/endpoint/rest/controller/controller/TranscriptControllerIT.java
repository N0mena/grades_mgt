package hei.school.minou.endpoint.rest.controller.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hei.school.minou.endpoint.event.EventProducer;
import hei.school.minou.endpoint.event.model.TranscriptRequested;
import hei.school.minou.entity.User;
import hei.school.minou.entity.enums.Role;
import hei.school.minou.exception.ForbiddenOperationException;
import hei.school.minou.security.SecurityUtils;
import hei.school.minou.service.GradeHistoryService;
import hei.school.minou.service.GradeService;
import hei.school.minou.service.GroupService;
import hei.school.minou.service.TranscriptService;
import hei.school.minou.service.UserService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TranscriptControllerIT {

  @Mock private EventProducer<TranscriptRequested> eventProducer;
  @Mock private GradeService gradeService;
  @Mock private UserService userService;
  @Mock private SecurityUtils securityUtils;
  @Mock private GradeHistoryService gradeHistoryService;
  @Mock private GroupService groupService;

  private TranscriptService transcriptService;
  private StudentController studentController;

  @BeforeEach
  void setUp() {
    transcriptService =
        new TranscriptService(gradeService, userService, securityUtils, eventProducer);
    studentController =
        new StudentController(
            securityUtils, gradeService, gradeHistoryService, groupService, transcriptService);
  }

  @Test
  void studentRequestingOwnTranscript_sendsEvent() {
    UUID studentId = UUID.randomUUID();
    User student = User.builder().id(studentId).role(Role.STUDENT).email("s@m.h").build();
    when(securityUtils.currentUser()).thenReturn(student);
    when(userService.getUserById(studentId)).thenReturn(student);

    studentController.sendMyTranscript();

    ArgumentCaptor<List<TranscriptRequested>> captor = ArgumentCaptor.forClass(List.class);
    verify(eventProducer).accept(captor.capture());
    assertThat(captor.getValue()).hasSize(1);
    assertThat(captor.getValue().get(0).getStudentId()).isEqualTo(studentId);
    assertThat(captor.getValue().get(0).getRecipientEmail()).isEqualTo("s@m.h");
  }

  @Test
  void adminRequestingAnyTranscript_sendsEvent() {
    UUID studentId = UUID.randomUUID();
    User admin = User.builder().id(UUID.randomUUID()).role(Role.ADMIN).email("admin@m.h").build();
    User student = User.builder().id(studentId).role(Role.STUDENT).email("s@m.h").build();
    when(securityUtils.currentUser()).thenReturn(admin);
    when(userService.getUserById(studentId)).thenReturn(student);

    studentController.sendStudentTranscript(studentId);

    ArgumentCaptor<List<TranscriptRequested>> captor = ArgumentCaptor.forClass(List.class);
    verify(eventProducer).accept(captor.capture());
    assertThat(captor.getValue().get(0).getStudentId()).isEqualTo(studentId);
    assertThat(captor.getValue().get(0).getRecipientEmail()).isEqualTo("s@m.h");
  }

  @Test
  void teacherRequestingAnyTranscript_throwsForbidden() {
    UUID studentId = UUID.randomUUID();
    User teacher = User.builder().id(UUID.randomUUID()).role(Role.TEACHER).email("t@m.h").build();

    assertThatThrownBy(() -> transcriptService.requestTranscriptSend(studentId, teacher))
        .isInstanceOf(ForbiddenOperationException.class);
  }

  @Test
  void studentRequestingOtherTranscript_throwsForbidden() {
    UUID studentId = UUID.randomUUID();
    User other = User.builder().id(UUID.randomUUID()).role(Role.STUDENT).email("other@m.h").build();

    assertThatThrownBy(() -> transcriptService.requestTranscriptSend(studentId, other))
        .isInstanceOf(ForbiddenOperationException.class)
        .hasMessage("Vous ne pouvez demander que votre propre relevé");
  }
}
