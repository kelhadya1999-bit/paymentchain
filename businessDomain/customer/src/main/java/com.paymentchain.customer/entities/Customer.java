package com.paymentchain.customer.entities;

import jakarta.persistence.*;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;

//utilizar la librerya lombok para generar los getters y setter
@Data
//indicar a jpa que la clase customer es una entidad para tratarla como tal
@Entity
public class Customer {
    @Id
    //esto hace que el id se autocomplete como un contador
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //esta linea hace que sagger esconda la variable id en el post
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    //indica el campo clave primaria
    private long id;
    private String firstName;
    private String code;
    private String phone;
    private String iban;
    private String surname;
    private String adress;

    //orphanRemoval = true asegura que si quitas un producto de la lista, se elimina de la BD
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    //Inicializar la lista evita problemas de duplicados o nulls
    private List<CustomerProduct> products = new ArrayList<>();

    // TRANSIT SIGNIFICA QUE ES UN PRODUCTO QUE NO SE VA A GUARDAR EN LA DB
    @Transient
    private List<?> transactions;


}
