package dev.mikablondo.loom_spring_playground;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LoomSpringPlaygroundApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoomSpringPlaygroundApplication.class, args);
	}

}
