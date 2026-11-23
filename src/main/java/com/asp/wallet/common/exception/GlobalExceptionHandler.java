package com.asp.wallet.common.exception;

import com.asp.wallet.appuser.exception.UserNotFoundException;
import com.asp.wallet.wallet.exception.InvalidWalletCommandException;
import com.asp.wallet.wallet.exception.WalletNotFoundException;
import com.asp.wallet.wallet.transactions.exception.InvalidWalletTransactionCommandException;
import com.asp.wallet.wallet.exception.WalletOperationException;
import com.asp.wallet.wallet.transactions.exception.WalletTransactionNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        log.error("Unexpected error while processing request: {}", request.getRequestURI(), ex);
        return buildResponse(status, "An unexpected error occurred.", request.getRequestURI());
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
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex,
                                                     HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return buildResponse(status, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWalletNotFound(WalletNotFoundException ex,
                                                       HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return buildResponse(status, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(InvalidWalletCommandException.class)
    public ResponseEntity<ErrorResponse> handleInvalidWalletCommandException(InvalidWalletCommandException ex,
                                                                             HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return buildResponse(status, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(WalletOperationException.class)
    public ResponseEntity<ErrorResponse> handleWalletOperationException(WalletOperationException ex,
                                                                        HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return buildResponse(status, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(InvalidWalletTransactionCommandException.class)
    public ResponseEntity<ErrorResponse> handleInvalidWalletTransactionCommandException(InvalidWalletTransactionCommandException ex,
                                                                                        HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return buildResponse(status, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(WalletTransactionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWalletTransactionNotFoundException(WalletTransactionNotFoundException ex,
                                                                                  HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return buildResponse(status, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLockingFailureException(OptimisticLockingFailureException ex,
                                                                                 HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        return buildResponse(
                status,
                "The resource was modified by another request. Please try again.",
                request.getRequestURI());
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
