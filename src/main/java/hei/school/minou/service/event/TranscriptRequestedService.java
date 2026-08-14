package hei.school.minou.service.event;

import static java.io.File.createTempFile;

import hei.school.minou.endpoint.event.model.TranscriptRequested;
import hei.school.minou.entity.Grade;
import hei.school.minou.entity.User;
import hei.school.minou.exception.ResourceNotFoundException;
import hei.school.minou.file.bucket.BucketComponent;
import hei.school.minou.mail.Email;
import hei.school.minou.mail.Mailer;
import hei.school.minou.mapper.UserMapper;
import hei.school.minou.repository.UserRepository;
import hei.school.minou.service.GradeService;
import hei.school.minou.service.PdfGenerationService;
import jakarta.mail.internet.InternetAddress;
import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TranscriptRequestedService implements Consumer<TranscriptRequested> {

  private static final Duration LINK_VALIDITY = Duration.ofDays(7);

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final GradeService gradeService;
  private final PdfGenerationService pdfGenerationService;
  private final BucketComponent bucketComponent;
  private final Mailer mailer;

  @SneakyThrows
  @Override
  public void accept(TranscriptRequested event) {
    User requester = findUser(event.getStudentId());
    List<Grade> grades = gradeService.getGradesByStudent(event.getStudentId(), requester);

    File pdfFile = createTempFile("transcript-" + event.getStudentId(), ".pdf");
    try {
      String studentName = requester.firstName() + " " + requester.lastName();
      pdfGenerationService.generateTranscript(studentName, grades, pdfFile);

      String bucketKey =
          "transcripts/" + event.getStudentId() + "-" + System.currentTimeMillis() + ".pdf";
      bucketComponent.upload(pdfFile, bucketKey);

      var presignedUri = bucketComponent.presign(bucketKey, LINK_VALIDITY);

      sendEmail(event.getRecipientEmail(), presignedUri.toString());
    } finally {
      pdfFile.delete();
    }
  }

  private User findUser(UUID userId) {
    return userRepository
        .findById(userId)
        .map(userMapper::toDomain)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
  }

  private void sendEmail(String recipientEmail, String downloadLink) throws Exception {
    var recipient = new InternetAddress(recipientEmail);
    var body =
        "Bonjour,\n\n"
            + "Votre relevé de notes est disponible au lien suivant :\n"
            + downloadLink
            + "\n\nCe lien expire dans 7 jours.\n\nCordialement.";

    mailer.accept(
        new Email(recipient, List.of(), List.of(), "Votre relevé de notes", body, List.of()));
  }
}
