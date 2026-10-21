package me.ihqqq.trohub.configuration;

import lombok.extern.slf4j.Slf4j;
import me.ihqqq.trohub.entity.User;
import me.ihqqq.trohub.enums.Role;
import me.ihqqq.trohub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Slf4j
public class ApplicationInitConfig {

    @Bean
    ApplicationRunner adminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder,
                                       @Value("${app.admin.username:admin}") String username,
                                       @Value("${app.admin.password:}") String password) {
        return args -> {
            if (password == null || password.isBlank()) {
                log.info("APP_ADMIN_PASSWORD chưa đặt -> bỏ qua tạo tài khoản admin");
                return;
            }
            if (userRepository.existsByUsername(username)) {
                return;
            }
            User admin = new User();
            admin.setUsername(username);
            admin.setPassword(passwordEncoder.encode(password));
            admin.setFullName("Quản trị hệ thống");
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
            log.warn("Đã tạo tài khoản admin '{}'", username);
        };
    }
}