package com.paymentchain.customer.businessTransaction;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;
@Data
@EqualsAndHashCode(callSuper = false)
public class BusinessRuleException extends Exception {
    public long id;
    public String code;
    public HttpStatus status;

    public BusinessRuleException(long id, String code, String message, HttpStatus status)
    {
        super(message);
        this.id = id;
        this.code = code;
        this.status = status;
    }
    public BusinessRuleException(long id, String message, HttpStatus status)
    {
       super(message);
        this.id = id;
        this.status = status;
    }
    public BusinessRuleException( String message, Throwable cause){
        super(message,cause);
    }
}
