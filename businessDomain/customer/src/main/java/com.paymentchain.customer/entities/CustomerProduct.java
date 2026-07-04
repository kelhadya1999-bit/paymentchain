package com.paymentchain.customer.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
//CustomerProduct NO es un producto, es una tabla intermedia.
public class CustomerProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private long id;
    private long productId;
    @Transient
    private String productName; //solo para devolver al frontend, no se guarda en ninguna parte

    @JsonIgnore
    //sirve para indicar la relación entre productos y clientes
    //Un cliente puede tener muchos productos
    // Un producto puede pertenecer a muchos clientes
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Customer.class)
    @JoinColumn(name = "customerId",nullable = true)
    private Customer customer;
}
