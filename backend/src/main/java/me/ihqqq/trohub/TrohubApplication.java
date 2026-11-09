package me.ihqqq.trohub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Arrays;
import java.util.Map;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class TrohubApplication {

	public static void main(String[] args) {
		boolean passwordResetRequested = Arrays.asList(args).contains("--admin-password-reset");
		if (!passwordResetRequested) {
			SpringApplication.run(TrohubApplication.class, args);
			return;
		}

		if (System.console() == null) {
			System.err.println("Không thể reset mật khẩu an toàn: console không khả dụng.");
			System.exit(2);
		}

		SpringApplication application = new SpringApplication(TrohubApplication.class);
		application.setWebApplicationType(WebApplicationType.NONE);
		application.setDefaultProperties(Map.of(
				"app.admin.password-reset-mode", "true",
				"spring.flyway.enabled", "false"));
		var context = application.run(args);
		int exitCode = SpringApplication.exit(context);
		System.exit(exitCode);
	}

}
