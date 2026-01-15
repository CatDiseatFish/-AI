package com.ym.ai_story_studio_server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AiStoryStudioServerApplication {

	@Bean
	CommandLineRunner printMarker(@Value("${my.marker:NOT_LOADED}") String marker) {
		return args -> System.out.println("CONFIG_MARKER=" + marker);
	}

	public static void main(String[] args) {
		SpringApplication.run(AiStoryStudioServerApplication.class, args);
	}
}
