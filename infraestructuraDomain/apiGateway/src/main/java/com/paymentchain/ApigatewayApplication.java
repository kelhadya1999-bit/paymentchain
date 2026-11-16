package com.paymentchain;

import org.springframework.boot.SpringApplication;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;


@SpringBootApplication
public class ApigatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApigatewayApplication.class, args);
    }
}