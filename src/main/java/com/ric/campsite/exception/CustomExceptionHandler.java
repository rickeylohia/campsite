package com.ric.campsite.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.format.DateTimeParseException;

@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(DateTimeParseException.class)
    public final ResponseEntity<Error> handleDateTimeParseException(DateTimeParseException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Error("Invalid date/format. Please provide date in dd-MMM-yyyy (eg. 28-Feb-2021)"));
    }

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<Error> handleException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Error("Internal error occurred"));
    }

    @ExceptionHandler(NotFoundException.class)
    public final ResponseEntity<Error> handleNotFoundException(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Error(ex.getMessage()));
    }

    @ExceptionHandler(CustomException.class)
    public final ResponseEntity<Error> handleCustomException(CustomException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Error(ex.getMessage()));
    }

    public static class Error {
        private String error;

        public Error(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }
}
