package com.paymentchain.customer.entities;

import jakarta.persistence.Entity;
import lombok.Data;

@Data
public class productDTO {
    //DTO te permite mapear la respuesta JSON de Product a un objeto Java temporal en memoria, sin tocar la base de datos.
    private Long id;
    private String name;
}
