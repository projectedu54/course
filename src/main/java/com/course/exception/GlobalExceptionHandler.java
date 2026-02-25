package com.course.exception;

import com.course.api.ApiResponse;
import com.course.exception.customException.*;
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

    // Resource not found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(false, ex.getMessage(), null));
    }

    //  Validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage())
                );

        return ResponseEntity
                .badRequest()
                .body(new ApiResponse<>(
                        false,
                        "Validation failed",
                        errors
                ));
    }

    // ⚠️ Illegal arguments
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(
            IllegalArgumentException ex) {

        return ResponseEntity
                .badRequest()
                .body(new ApiResponse<>(false, ex.getMessage(), null));
    }

    // Fallback (unexpected errors)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(
                        false,
                        "Something went wrong. Please try again later.",
                        null
                ));
    }

    @ExceptionHandler(InvalidCourseStructureException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCourseStructure(
            InvalidCourseStructureException ex) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(
                        false,
                        ex.getMessage(),
                        null
                ));
    }

    @ExceptionHandler(InvalidContentException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidContent(InvalidContentException ex) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(
                        false,
                        ex.getMessage(),
                        null
                ));
    }

    // Handle CourseServiceException with custom status
    @ExceptionHandler(CourseServiceException.class)
    public ResponseEntity<ApiResponse<Void>> handleCourseServiceException(CourseServiceException ex) {
        // Use the status provided by the exception
        return ResponseEntity
                .status(ex.getStatus())
                .body(new ApiResponse<>(
                        false,
                        ex.getMessage(),
                        null
                ));
    }

    @ExceptionHandler(DuplicateTopicTitleException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateTopicTitle(DuplicateTopicTitleException ex) {
        return ResponseEntity
                .status(ex.getStatus())
                .body(new ApiResponse<>(false, ex.getMessage(), null));
    }

    // Handle DuplicateTitleException
    @ExceptionHandler(DuplicateTitleException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateTitle(DuplicateTitleException ex) {
        return ResponseEntity
                .status(ex.getStatus())
                .body(new ApiResponse<>(false, ex.getMessage(), null));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidEnum(HttpMessageNotReadableException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(
                        false,
                        "Invalid content type. Allowed values: TEXT, PDF, PPT, QUIZ, AUDIO, LINK, IMAGE, VIDEO",
                        null
                ));

    }




}
