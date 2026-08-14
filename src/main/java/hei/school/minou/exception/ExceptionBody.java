package hei.school.minou.exception;

import java.time.Instant;

public record ExceptionBody(String type, String message, Instant timestamp) {}
