package hei.school.minou.endpoint.event.model;

import java.time.Duration;
import java.util.UUID;
import lombok.*;

@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@ToString
@Getter
@Setter
public class TranscriptRequested extends PojaEvent {
  private UUID studentId;
  private String recipientEmail;

  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofSeconds(45);
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofSeconds(30);
  }
}
