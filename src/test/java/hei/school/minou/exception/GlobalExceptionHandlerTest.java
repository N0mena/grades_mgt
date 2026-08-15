package hei.school.minou.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void should_return_400_for_bad_request_exception() {
        ResponseEntity<ExceptionBody> response =
                handler.handleBadRequest(new BadRequestException("Invalid email or password"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("Invalid email or password");
    }

    @Test
    void should_return_401_for_invalid_token_exception() {
        ResponseEntity<ExceptionBody> response =
                handler.handleInvalidToken(new InvalidTokenException("Invalid token"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}