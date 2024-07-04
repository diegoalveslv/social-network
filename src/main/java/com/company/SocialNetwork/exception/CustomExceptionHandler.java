package com.company.SocialNetwork.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@ControllerAdvice
@Slf4j
public class CustomExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class, jakarta.validation.ConstraintViolationException.class})
    @ResponseStatus(BAD_REQUEST)
    public ResponseEntity<CustomErrorResponse> handleValidationExceptions(Exception ex) {

        List<String> messages = new ArrayList<>();
        if (ex instanceof MethodArgumentNotValidException mae) {
            mae.getBindingResult().getAllErrors().forEach((error) -> {
                String fieldName = ((FieldError) error).getField();
                String errorMessage = error.getDefaultMessage();
                messages.add(format("%s: %s", fieldName, errorMessage));
            });
        } else if (ex instanceof jakarta.validation.ConstraintViolationException cve) {
            cve.getConstraintViolations().forEach((cv) -> {
                String[] propertySlices = cv.getPropertyPath().toString().split("\\.");
                String fieldName = propertySlices[propertySlices.length - 1];
                String errorMessage = cv.getMessage();
                messages.add(format("%s: %s", fieldName, errorMessage));
            });
        } else {
            var invalidExceptionTypeEx = new RuntimeException("Invalid exception type.");
            log.error("Invalid exception type: " + ex.getClass().getName(), invalidExceptionTypeEx);
            throw invalidExceptionTypeEx;
        }

        var errorResponse = new CustomErrorResponse(BAD_REQUEST.value(), messages);

        return new ResponseEntity<>(errorResponse, BAD_REQUEST);
    }

    @ExceptionHandler(UnprocessableEntityException.class)
    @ResponseStatus(UNPROCESSABLE_ENTITY)
    public ResponseEntity<CustomErrorResponse> handleUnprocessableEntityExceptions(UnprocessableEntityException ex) {
        var response = new CustomErrorResponse(UNPROCESSABLE_ENTITY.value(), ex.getMessages());

        return new ResponseEntity<>(response, UNPROCESSABLE_ENTITY);
    }
}
