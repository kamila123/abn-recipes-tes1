package com.abn.recipes.service.exception;

import com.abn.recipes.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class RestExceptionHandler {

    @ExceptionHandler(ResourceAlreadyExistException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    protected ErrorResponse handleResourceAlreadyExistException(ResourceAlreadyExistException ex) {
        return new ErrorResponse(ex);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    protected ErrorResponse handleNotFound(ResourceNotFoundException ex) {
        return new ErrorResponse(ex);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected ErrorResponse handleInternalServerErrorRequest(Exception ex) {
        log.error(" abn-recipes error :", ex);
        return new ErrorResponse(ex);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = new ArrayList<String>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + " : " + error.getDefaultMessage());
        }
        String errorMessage = errors.stream().map(Object::toString).collect(Collectors.joining(","));
        return new ErrorResponse(errorMessage);
    }
}
