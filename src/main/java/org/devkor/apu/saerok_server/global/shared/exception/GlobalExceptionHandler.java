package org.devkor.apu.saerok_server.global.shared.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        ErrorResponse error = new ErrorResponse(500, ex.getMessage());
        return ResponseEntity.
                internalServerError().
                body(error);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(BadRequestException ex) {
        ErrorResponse error = new ErrorResponse(400, ex.getMessage());
        return ResponseEntity.
                badRequest().
                body(error);
    }
    
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenException(ForbiddenException ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.FORBIDDEN.value(), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(error);
    }

    @ExceptionHandler(S3DeleteException.class)
    public ResponseEntity<ErrorResponse> handleS3DeleteException(S3DeleteException ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(error);
    }

    @ExceptionHandler(OAuthException.class)
    public ResponseEntity<ErrorResponse> handleOAuthException(OAuthException ex) {
        ErrorResponse error = new ErrorResponse(ex.getStatusCode(), ex.getMessage());
        HttpStatus httpStatus = HttpStatus.resolve(ex.getStatusCode());
        return ResponseEntity
                .status(httpStatus != null ? httpStatus : HttpStatus.UNAUTHORIZED)
                .body(error);
    }
}
