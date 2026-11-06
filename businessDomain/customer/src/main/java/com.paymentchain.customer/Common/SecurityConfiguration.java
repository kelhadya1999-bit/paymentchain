package com.paymentchain.customer.Common;

import jakarta.ws.rs.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    private static final String[] NO_AUTH_LIST = {
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/configuration/ui",
            "/swagger-resources/**",
            "/configuration/security",
            "/webjars/**",
            "/login",
            "/h2-console/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

//        http.csrf(csrf -> csrf.disable())
//                .authorizeHttpRequests(auth -> auth
//                        .anyRequest().authenticated()
//                )
//                .httpBasic(Customizer.withDefaults())
//                .formLogin(Customizer.withDefaults());

        //todas las peticiones get que tengan en su url customer tendrán que estar autentificadas
        //todos los post además de estar autentificados solo permitirá el usuario admin
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(NO_AUTH_LIST).permitAll()
                        .requestMatchers(HttpMethod.POST, "/customer/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/customer/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .formLogin(Customizer.withDefaults());

        return http.build();
    }

    @Bean
     CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration cc = new CorsConfiguration();
        //cabeceras http
        cc.setAllowedHeaders(Arrays.asList("Origin", "Accept", "X-Requested-With", "Content-Type",
                "Access-Control-Request-Method", "Access-Control-Request-Headers", "Authorization"));
        cc.setExposedHeaders(Arrays.asList(  "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"));

        // Sustituye por el origen real de tu frontend
        cc.setAllowedOrigins(Arrays.asList("http://localhost:4200" ));
        cc.setAllowedMethods(Arrays.asList( "GET", "POST", "OPTIONS", "PUT", "PATCH", "DELETE"));

        cc.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cc);

        return source;
     }
}
