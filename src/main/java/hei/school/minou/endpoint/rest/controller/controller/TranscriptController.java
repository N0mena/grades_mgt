package hei.school.minou.endpoint.rest.controller.controller;

import hei.school.minou.endpoint.event.EventProducer;
import hei.school.minou.endpoint.event.model.TranscriptRequested;
import hei.school.minou.entity.enums.Role;
import hei.school.minou.exception.ForbiddenOperationException;
import hei.school.minou.service.auth.AuthPrincipal;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/students")
@AllArgsConstructor
public class TranscriptController {

  private final EventProducer<TranscriptRequested> eventProducer;

  @PostMapping("/{studentId}/transcript")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void requestTranscript(@PathVariable UUID studentId) {
    AuthPrincipal principal = currentUser();

    boolean isOwner = principal.userId().equals(studentId);
    boolean isPrivileged = principal.role() == Role.ADMIN || principal.role() == Role.TEACHER;

    if (!isOwner && !isPrivileged) {
      throw new ForbiddenOperationException("Vous ne pouvez demander que votre propre relevé");
    }

    var event =
        TranscriptRequested.builder()
            .studentId(studentId)
            .recipientEmail(principal.email())
            .build();

    eventProducer.accept(List.of(event));
  }

  private AuthPrincipal currentUser() {
    return (AuthPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }
}
