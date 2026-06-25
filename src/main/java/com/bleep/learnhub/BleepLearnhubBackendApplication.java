package com.bleep.learnhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BleepLearnhubBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BleepLearnhubBackendApplication.class, args);
	}

}
