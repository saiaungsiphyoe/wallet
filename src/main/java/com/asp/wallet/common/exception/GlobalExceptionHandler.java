package com.asp.wallet.common.exception;

import com.asp.wallet.appuser.exception.UserNotFoundException;
import com.asp.wallet.wallet.exception.InvalidWalletCommandException;
import com.asp.wallet.wallet.exception.WalletNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return buildResponse(status, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        return buildResponse(status, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex,
                                                            HttpServletRequest request) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        return buildResponse(status, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(Exception ex,
                                                     HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return buildResponse(status, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWalletNotFound(Exception ex,
                                                       HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return buildResponse(status, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(InvalidWalletCommandException.class)
    public ResponseEntity<ErrorResponse> handleInvalidWalletCommandException(Exception ex,
                                                                             HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return buildResponse(status, ex.getMessage(), request.getRequestURI());
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message, String path) {
        ErrorResponse response = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                path
        );
        return ResponseEntity.status(status).body(response);
    }
}
