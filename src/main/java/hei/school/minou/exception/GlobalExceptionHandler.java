package hei.school.minou.exception;

import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ExceptionBody> handleBadRequest(BadRequestException e) {
    return respond(HttpStatus.BAD_REQUEST, "BAD_REQUEST", e.getMessage());
  }

  @ExceptionHandler(ForbiddenOperationException.class)
  public ResponseEntity<ExceptionBody> handleForbidden(ForbiddenOperationException e) {
    return respond(HttpStatus.FORBIDDEN, "FORBIDDEN_OPERATION", e.getMessage());
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ExceptionBody> handleNotFound(ResourceNotFoundException e) {
    return respond(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", e.getMessage());
  }

  @ExceptionHandler(InvalidTokenException.class)
  public ResponseEntity<ExceptionBody> handleInvalidToken(InvalidTokenException e) {
    return respond(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", e.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ExceptionBody> handleValidation(MethodArgumentNotValidException e) {
    String message =
        e.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(
                error ->
                    error.getField()
                        + ": "
                        + (error.getDefaultMessage() == null
                            ? "invalid value"
                            : error.getDefaultMessage()))
            .orElse("Invalid request payload");
    return respond(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", message);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ExceptionBody> handleGeneric(Exception e) {
    return respond(
        HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "An unexpected error occurred");
  }

  private ResponseEntity<ExceptionBody> respond(HttpStatus status, String type, String message) {
    return ResponseEntity.status(status).body(new ExceptionBody(type, message, Instant.now()));
  }
}
