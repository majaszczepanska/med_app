package com.maja.med_app.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;


import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    //ACCESS RULES
    //define the security filter chain (who has access to what endpoints)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            //.cors(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/auth/register", "/auth/login", "/auth/verify").permitAll()
                .anyRequest().authenticated()
                //.anyRequest().permitAll()
            )
            .httpBasic(Customizer.withDefaults());
        return http.build();  
    }

    //CORS (Cross-Origin Resource Sharing)
    //CORS configuration to allow requests from Angular frontend (its safe to send requests from localhost:4200 to localhost:8080)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        //configuration.setAllowedOrigins(List.of("http://192.168.131.213:4200"));
        configuration.setAllowedOrigins(List.of("http://localhost:4200", "http://192.168.131.213:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


    //Bean for password encoding (hashing passwords before saving to db)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
        //return NoOpPasswordEncoder.getInstance();
    }





    //create hash 
    /*
    @Bean
    public org.springframework.boot.CommandLineRunner printHash(PasswordEncoder encoder) {
        return args -> {
            System.out.println("--- 🔑 TO JEST TWÓJ HASH DLA admin123 🔑 ---");
            // To wygeneruje hash idealnie pasujący do Twojej wersji Springa
            System.out.println(encoder.encode("patient")); 
            System.out.println("---------------------------------------------");
        };
    }
    */
}
