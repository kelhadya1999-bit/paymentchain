package com.paymentchain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class EurecaServerApplication {

    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(EurecaServerApplication.class, args);
       // SpringApplication.run(EurecaServerApplication.class, args);
    }
}