package com.headspace;

import com.headspace.identity.infrastructure.security.properties.SecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
public class HeadspaceApplication {

	public static void main(String[] args) {
		SpringApplication.run(HeadspaceApplication.class, args);
	}

}
