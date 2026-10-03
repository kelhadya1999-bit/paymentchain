package com.paymentchain;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@EnableConfigServer
@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(Main.class, args);
    }
}