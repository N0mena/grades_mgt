package hei.school.minou.conf;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class JwtProperties {

  private final String secret;
  private final long expirationMs;

  public JwtProperties(
      @Value("${JWT_SECRET:change-me-change-me-change-me-change-me-123456}") String secret,
      @Value("${JWT_EXPIRATION_MS:86400000}") long expirationMs) {
    this.secret = secret;
    this.expirationMs = expirationMs;
  }
}
