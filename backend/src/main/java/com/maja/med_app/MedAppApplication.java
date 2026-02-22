package com.maja.med_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MedAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(MedAppApplication.class, args);
		
	}

}
