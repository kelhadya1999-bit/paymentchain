package com.paymentchain.customer.Exceptions;

import com.paymentchain.customer.Common.StandarizedApiExceptionResponse;
import com.paymentchain.customer.businessTransaction.BusinessRuleException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    //esta escepción salta cuando no hay comunicacón con el servicio productos o transacciones
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception ex) {
        StandarizedApiExceptionResponse standarizedApiExceptionResponse =
                new StandarizedApiExceptionResponse("Tecnico","Error de entrada","124", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(standarizedApiExceptionResponse);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<?> handleException(BusinessRuleException ex) {
        StandarizedApiExceptionResponse standarizedApiExceptionResponse =
                new StandarizedApiExceptionResponse("Business","Error de validación",ex.getCode(), ex.getMessage());
            return ResponseEntity.status(ex.getStatus()).body(standarizedApiExceptionResponse);
    }
}
