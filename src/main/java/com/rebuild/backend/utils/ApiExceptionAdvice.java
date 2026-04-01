package com.rebuild.backend.utils;


import com.rebuild.backend.model.exceptions.ApiException;
import com.rebuild.backend.model.responses.ApiErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ApiExceptionAdvice {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(ApiException apiException)
    {
        return ResponseEntity.status(apiException.getStatus()).body(new ApiErrorResponse(
                apiException.getStatusCode(), apiException.getMessage()
        ));
    }
}
