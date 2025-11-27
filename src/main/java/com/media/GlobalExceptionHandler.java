// src/main/java/com/media/GlobalExceptionHandler.java
package com.media;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<?> handleBadRequest(Exception e) {
        return ResponseEntity.badRequest().body(err(e.getMessage(), HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler({BadCredentialsException.class})
    public ResponseEntity<?> handleAuth(Exception e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(err(e.getMessage(), HttpStatus.UNAUTHORIZED));
    }

    @ExceptionHandler({AccessDeniedException.class})
    public ResponseEntity<?> handleForbidden(Exception e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(err(e.getMessage(), HttpStatus.FORBIDDEN));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + " " + f.getDefaultMessage())
                .findFirst().orElse("Validation error");
        return ResponseEntity.badRequest().body(err(msg, HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleOther(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(err("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR));
    }

    private Map<String, Object> err(String msg, HttpStatus status) {
        return Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", msg
        );
    }
}
