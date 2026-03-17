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
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ================= 1. Resource Not Found =================
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
        logger.error("Resource not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(false, ex.getMessage(), null));
    }

    // ================= 2. Validation Errors (Field Level) =================
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
                .body(new ApiResponse<>(false, "Validation failed", errors));
    }

    // ================= 3. JSON / Enum Parsing Errors =================
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex) {

        String mostSpecificMessage = ex.getMostSpecificCause().getMessage();
        logger.error("JSON parse error: {}", mostSpecificMessage);

        String userFriendlyMessage = "Invalid request body format.";

        if (mostSpecificMessage.contains("com.course.enums.LevelType")) {
            userFriendlyMessage = "Invalid levelType. Allowed values: TEXT, PDF, PPT, QUIZ, AUDIO, LINK, IMAGE, VIDEO";
        } else if (mostSpecificMessage.contains("java.util.LinkedHashMap")) {
            userFriendlyMessage = "Invalid metadata format. Metadata must be a JSON object, not a string.";
        } else {
            userFriendlyMessage = "JSON parse error: " + mostSpecificMessage;
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, userFriendlyMessage, null));
    }

    // ================= 4. URL Parameter Mismatch (Enums in @RequestParam) =================
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("The parameter '%s' has an invalid value. Allowed values are: TEXT, PDF, PPT, QUIZ, AUDIO, LINK, IMAGE, VIDEO",
                ex.getName());

        logger.error("Type mismatch error: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, message, null));
    }

    // ================= 5. Specific Custom Exceptions =================
    @ExceptionHandler(InvalidCourseStructureException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCourseStructure(InvalidCourseStructureException ex) {
        logger.error("Invalid course structure: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(false, ex.getMessage(), null));
    }

    @ExceptionHandler(InvalidContentException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidContent(InvalidContentException ex) {
        logger.error("Invalid content: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(false, ex.getMessage(), null));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(BadRequestException ex) {
        logger.error("Bad Request: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(false, ex.getMessage(), null));
    }

    @ExceptionHandler(CourseServiceException.class)
    public ResponseEntity<ApiResponse<Void>> handleCourseServiceException(CourseServiceException ex) {
        logger.error("Course service exception: {}", ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(new ApiResponse<>(false, ex.getMessage(), null));
    }

    // ================= 6. Common Java Exceptions =================
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        logger.error("Illegal argument: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(new ApiResponse<>(false, ex.getMessage(), null));
    }

    // ================= 7. Fallback (Final Catch-All) =================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
        logger.error("Unexpected error occurred", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Something went wrong. Please try again later.", null));
    }
}