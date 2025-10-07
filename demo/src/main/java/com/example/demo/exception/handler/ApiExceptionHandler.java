package com.example.demo.exception.handler;

import com.example.demo.exception.base.AppException;
import com.example.demo.exception.types.InActiveException;
import com.example.demo.exception.types.InvalidOperationException;
import com.example.demo.exception.types.InvalidRoleException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.management.OperationsException;
import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ApiExceptionHandler {


    @ExceptionHandler(AppException.class)
    @ResponseStatus
    public ProblemDetail handleApp(AppException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(ex.getStatus());
        pd.setTitle(ex.getClass().getSimpleName());
        pd.setDetail(ex.getMessage());
        pd.setProperty("code", ex.getCode());
        pd.setProperty("path", req.getRequestURI());
        return pd;
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String,String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect( Collectors.toMap( FieldError::getField,
                        fe -> fe.getDefaultMessage() == null ? "Invalid value" : fe.getDefaultMessage(),
                        (a,b) -> a));
        ProblemDetail pd = ProblemDetail.forStatus( HttpStatus.BAD_REQUEST);
        pd.setTitle("Validation failed");
        pd.setDetail("One or more fields are invalid.");
        pd.setProperty("errors", errors);
        pd.setProperty("path", req.getRequestURI());
        return pd;
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    @ResponseStatus
    public ProblemDetail handleIllegalArguments(RuntimeException ex, HttpServletRequest req) {


        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle(ex.getClass().getSimpleName());
        pd.setDetail(ex.getMessage());
        pd.setProperty("path", req.getRequestURI());
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(InvalidRoleException.class)
    @ResponseStatus
    public ProblemDetail handleRole(AppException ex, HttpServletRequest req) {


        ProblemDetail pd = ProblemDetail.forStatus(ex.getStatus());
        pd.setTitle(ex.getClass().getSimpleName());
        pd.setDetail(ex.getMessage());
        pd.setProperty("path", req.getRequestURI());
        pd.setProperty("timestamp", Instant.now());
        pd.setProperty ( "code", ex.getCode() );
        return pd;
    }


    @ExceptionHandler({InvalidOperationException.class, OperationsException.class})
    @ResponseStatus
    public ProblemDetail handleOperation(AppException ex, HttpServletRequest req) {


        ProblemDetail pd = ProblemDetail.forStatus(ex.getStatus());
        pd.setTitle(ex.getClass().getSimpleName());
        pd.setDetail(ex.getMessage());
        pd.setProperty("path", req.getRequestURI());
        pd.setProperty("timestamp", Instant.now());
        pd.setProperty ( "code", ex.getCode() );
        return pd;
    }


    @ExceptionHandler(InActiveException.class)
    @ResponseStatus
    public ProblemDetail handleInActive(AppException ex, HttpServletRequest req) {


        ProblemDetail pd = ProblemDetail.forStatus(ex.getStatus());
        pd.setTitle(ex.getClass().getSimpleName());
        pd.setDetail(ex.getMessage());
        pd.setProperty("path", req.getRequestURI());
        pd.setProperty("timestamp", Instant.now());
        pd.setProperty ( "code", ex.getCode() );
        return pd;
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(org.springframework.security.access.AccessDeniedException ex,
                                            HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        pd.setTitle("Access Denied");
        pd.setDetail("You don't have permission to access this resource");
        pd.setProperty("path", req.getRequestURI());
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus
    public ProblemDetail handleUnknown(Exception ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setTitle("Internal server error");
        pd.setDetail(ex.getMessage ());
        pd.setProperty("path", req.getRequestURI());
        return pd;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus
    public ProblemDetail handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Malformed JSON request");
        pd.setDetail(ex.getMessage ());
        pd.setProperty("path", req.getRequestURI());
        return pd;
    }


}