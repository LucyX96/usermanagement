package com.company.usermanagement;

import com.company.usermanagement.configuration.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class UsermanagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(UsermanagementApplication.class, args);
	}

}
