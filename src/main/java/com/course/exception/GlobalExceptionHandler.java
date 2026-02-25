package com.course.exception;

import com.course.api.ApiResponse;
import com.course.exception.customException.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ================= Resource Not Found =================
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {

        logger.error("Resource not found: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(false, ex.getMessage(), null));
    }

    // ================= Validation Errors =================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage())
                );

        logger.error("Validation failed: {}", errors);

        return ResponseEntity
                .badRequest()
                .body(new ApiResponse<>(
                        false,
                        "Validation failed",
                        errors
                ));
    }

    // ================= Illegal Arguments =================
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(
            IllegalArgumentException ex) {

        logger.error("Illegal argument: {}", ex.getMessage());

        return ResponseEntity
                .badRequest()
                .body(new ApiResponse<>(false, ex.getMessage(), null));
    }

    // ================= Invalid Course Structure =================
    @ExceptionHandler(InvalidCourseStructureException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCourseStructure(
            InvalidCourseStructureException ex) {

        logger.error("Invalid course structure: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, ex.getMessage(), null));
    }

    // ================= Invalid Content =================
    @ExceptionHandler(InvalidContentException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidContent(
            InvalidContentException ex) {

        logger.error("Invalid content: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, ex.getMessage(), null));
    }

    // ================= Course Service Exception =================
    @ExceptionHandler(CourseServiceException.class)
    public ResponseEntity<ApiResponse<Void>> handleCourseServiceException(
            CourseServiceException ex) {

        logger.error("Course service exception (status={}): {}",
                ex.getStatus(), ex.getMessage());

        return ResponseEntity
                .status(ex.getStatus())
                .body(new ApiResponse<>(false, ex.getMessage(), null));
    }

    // ================= Duplicate Topic =================
    @ExceptionHandler(DuplicateTopicTitleException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateTopicTitle(
            DuplicateTopicTitleException ex) {

        logger.error("Duplicate topic title: {}", ex.getMessage());

        return ResponseEntity
                .status(ex.getStatus())
                .body(new ApiResponse<>(false, ex.getMessage(), null));
    }

    // ================= Duplicate Title =================
    @ExceptionHandler(DuplicateTitleException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateTitle(
            DuplicateTitleException ex) {

        logger.error("Duplicate title: {}", ex.getMessage());

        return ResponseEntity
                .status(ex.getStatus())
                .body(new ApiResponse<>(false, ex.getMessage(), null));
    }

    // ================= Invalid Enum / JSON =================
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidEnum(
            HttpMessageNotReadableException ex) {

        logger.error("JSON parse error: {}", ex.getMostSpecificCause().getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(
                        false,
                        "Invalid content type. Allowed values: TEXT, PDF, PPT, QUIZ, AUDIO, LINK, IMAGE, VIDEO",
                        null
                ));
    }

    // ================= Fallback (Unexpected Errors) =================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {

        logger.error("Unexpected error occurred", ex);  // full stacktrace

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(
                        false,
                        "Something went wrong. Please try again later.",
                        null
                ));
    }
}