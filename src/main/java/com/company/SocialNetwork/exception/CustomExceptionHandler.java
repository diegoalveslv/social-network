package com.company.SocialNetwork.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@ControllerAdvice
@Slf4j
public class CustomExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class
            , jakarta.validation.ConstraintViolationException.class
            , FieldValidationException.class
            , FieldListValidationException.class})
    public ResponseEntity<CustomErrorResponse> handleValidationExceptions(Exception ex) {

        List<String> messages = new ArrayList<>();
        if (ex instanceof MethodArgumentNotValidException mae) {
            mae.getBindingResult().getAllErrors().forEach((error) -> {
                String fieldName = ((FieldError) error).getField();
                String errorMessage = error.getDefaultMessage();
                messages.add("%s: %s".formatted(fieldName, errorMessage));
            });
        } else if (ex instanceof jakarta.validation.ConstraintViolationException cve) {
            cve.getConstraintViolations().forEach((cv) -> {
                String[] propertySlices = cv.getPropertyPath().toString().split("\\.");
                String fieldName = propertySlices[propertySlices.length - 1];
                String errorMessage = cv.getMessage();
                messages.add("%s: %s".formatted(fieldName, errorMessage));
            });
        } else if (ex instanceof FieldValidationException cfe) {
            String fieldName = cfe.getFieldName();
            String errorMessage = cfe.getErrorMessage();
            messages.add("%s: %s".formatted(fieldName, errorMessage));
        } else if (ex instanceof FieldListValidationException flve) {
            Map<String, String> messageToFieldName = flve.getMessageToFieldName();
            messageToFieldName.forEach((fieldName, message) -> {
                messages.add("%s: %s".formatted(fieldName, message));
            });
        } else {
            var invalidExceptionTypeEx = new RuntimeException("Invalid exception type.");
            log.error("Invalid exception type: " + ex.getClass().getName(), invalidExceptionTypeEx);
            throw invalidExceptionTypeEx;
        }

        var errorResponse = new CustomErrorResponse(UNPROCESSABLE_ENTITY.value(), ZonedDateTime.now(), messages);

        return new ResponseEntity<>(errorResponse, UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler({NotFoundException.class})
    public ResponseEntity<?> handleNotFoundException(NotFoundException ex) {
        return new ResponseEntity<>(new CustomErrorResponse(NOT_FOUND.value(), ZonedDateTime.now(), List.of(ex.getMessage())), NOT_FOUND);
    }
}
