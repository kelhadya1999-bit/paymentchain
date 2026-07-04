package com.paymentchain.customer.entities;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TransactionDTO {
    private String reference;
    private String ibanAccount;
    private LocalDateTime date;
    private double amount ;
    private double fee;
    private String description;
    private String status;
    private String channel;
}
