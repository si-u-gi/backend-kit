package dev.siugi.backendkit.kit_auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class KitAuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(KitAuthApplication.class, args);
	}

	@RestController
	static class HealthController {

		@GetMapping("/health")
		public String health() {
			return "kit-auth server is running!";
		}
	}
}