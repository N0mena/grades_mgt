package hei.school.minou.service.auth;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import hei.school.minou.conf.JwtProperties;
import hei.school.minou.entity.User;
import hei.school.minou.entity.enums.Role;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class JwtService {

  private final JwtProperties jwtProperties;

  public String generateToken(User user) {
    try {
      Date now = new Date();
      JWTClaimsSet claims =
          new JWTClaimsSet.Builder()
              .subject(user.id().toString())
              .claim("role", user.role().name())
              .claim("email", user.email())
              .issueTime(now)
              .expirationTime(new Date(now.getTime() + jwtProperties.getExpirationMs()))
              .build();
      SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
      signedJWT.sign(new MACSigner(secretBytes()));
      return signedJWT.serialize();
    } catch (Exception e) {
      throw new RuntimeException("Failed to generate token", e);
    }
  }

  public AuthPrincipal parseToken(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);
      if (!signedJWT.verify(new MACVerifier(secretBytes()))) {
        throw new RuntimeException("Invalid token");
      }
      JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

      return new AuthPrincipal(
          UUID.fromString(claims.getSubject()),
          Role.valueOf(claims.getStringClaim("role")),
          claims.getStringClaim("email"));
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse token", e);
    }
  }

  private byte[] secretBytes() {
    return jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
  }
}
