package com.aft.api;

import org.springframework.boot.SpringApplication;

public class TestApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(AftApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
