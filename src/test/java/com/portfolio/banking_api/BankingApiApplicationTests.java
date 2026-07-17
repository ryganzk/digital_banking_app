package com.portfolio.banking_api;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

class BankingApiApplicationTests {

	@Test
	void main_delegatesToSpringApplication() {
		String[] args = new String[] {};

		try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
			springApplication
                .when(() -> SpringApplication.run(eq(BankingApiApplication.class), any(String[].class)))
                .thenReturn(null);

			BankingApiApplication.main(args);

			springApplication.verify(
                () -> SpringApplication.run(BankingApiApplication.class, args));
		}
	}
}
