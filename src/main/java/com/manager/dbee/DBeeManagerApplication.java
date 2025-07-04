package com.manager.dbee;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DBeeManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DBeeManagerApplication.class, args);
	}

}
