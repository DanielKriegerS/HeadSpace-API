package com.danielks.headspace;

import org.springframework.boot.SpringApplication;

public class TestHeadspaceApplication {

	public static void main(String[] args) {
		SpringApplication.from(HeadspaceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
