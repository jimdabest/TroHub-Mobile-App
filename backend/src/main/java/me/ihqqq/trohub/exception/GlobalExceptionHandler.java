package me.ihqqq.trohub.exception;

import me.ihqqq.trohub.dto.response.ApiResponse;
import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> handlingException(Exception exception) {
        if (exception instanceof ErrorResponse errorResponse) {
            ErrorCode code = ErrorCode.INVALID_REQUEST;
            return ResponseEntity.status(errorResponse.getStatusCode())
                    .body(ApiResponse.<Void>builder().code(code.getCode()).message(code.getMessage()).build());
        }
        log.error("Unhandled exception", exception);
        return build(ErrorCode.UNCATEGORIZED_EXCEPTION, ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage());
    }

    @ExceptionHandler(AppException.class)
    ResponseEntity<ApiResponse<Void>> handlingAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        return build(errorCode, errorCode.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiResponse<Void>> handlingAccessDenied(AccessDeniedException exception) {
        return build(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiResponse<Void>> handlingUnreadable(HttpMessageNotReadableException exception) {
        return build(ErrorCode.INVALID_REQUEST, ErrorCode.INVALID_REQUEST.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Void>> handlingValidation(MethodArgumentNotValidException exception) {
        ErrorCode errorCode = ErrorCode.INVALID_KEY;
        String message = errorCode.getMessage();

        var fieldError = exception.getBindingResult().getFieldError();
        if (fieldError != null && fieldError.getDefaultMessage() != null) {
            try {
                errorCode = ErrorCode.valueOf(fieldError.getDefaultMessage());
                message = errorCode.getMessage();
                Map<String, Object> attributes = fieldError.unwrap(ConstraintViolation.class)
                        .getConstraintDescriptor().getAttributes();
                message = fillAttributes(message, attributes);
            } catch (IllegalArgumentException e) {
                log.debug("Validation message is not an ErrorCode key: {}", fieldError.getDefaultMessage());
            }
        }
        return build(errorCode, message);
    }

    private static String fillAttributes(String message, Map<String, Object> attributes) {
        for (String key : new String[]{"min", "max"}) {
            if (attributes.containsKey(key)) {
                message = message.replace("{" + key + "}", String.valueOf(attributes.get(key)));
            }
        }
        return message;
    }

    private static ResponseEntity<ApiResponse<Void>> build(ErrorCode errorCode, String message) {
        return ResponseEntity.status(errorCode.getStatusCode())
                .body(ApiResponse.<Void>builder().code(errorCode.getCode()).message(message).build());
    }
}