package com.upc.ven_con_plata_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class VenConPlataBackendApplication {

	public static void main(String[] args) {

		SpringApplication.run(VenConPlataBackendApplication.class, args);

	}

}
