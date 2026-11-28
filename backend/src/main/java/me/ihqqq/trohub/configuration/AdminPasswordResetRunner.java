package me.ihqqq.trohub.configuration;

import lombok.RequiredArgsConstructor;
import me.ihqqq.trohub.service.AdminPasswordResetService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.Console;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.admin.password-reset-mode", havingValue = "true")
public class AdminPasswordResetRunner implements ApplicationRunner {

    private static final int BCRYPT_MAX_BYTES = 72;

    private final AdminPasswordResetService resetService;
    private final ApplicationContext applicationContext;
    private final Environment environment;

    @Override
    public void run(ApplicationArguments args) {
        if (!args.containsOption("admin-password-reset")
                || !"true".equals(environment.getProperty("app.admin.password-reset-mode"))) {
            throw new IllegalStateException("Chế độ reset mật khẩu chỉ được phép kích hoạt bằng flag riêng.");
        }
        if (applicationContext.containsBean("adminInitializer")) {
            throw new IllegalStateException("Không thể bảo đảm adminInitializer bị vô hiệu hóa.");
        }

        Console console = System.console();
        if (console == null) {
            throw new IllegalStateException("Console không khả dụng; không thực hiện reset.");
        }

        char[] password = console.readPassword("Nhập mật khẩu mới: ");
        if (password == null) {
            throw new IllegalStateException("Không đọc được mật khẩu; không thực hiện reset.");
        }
        char[] confirmation = null;
        try {
            validatePassword(password);
            confirmation = console.readPassword("Nhập lại mật khẩu mới: ");
            if (confirmation == null) {
                throw new IllegalStateException("Không đọc được xác nhận; không thực hiện reset.");
            }
            if (!Arrays.equals(password, confirmation)) {
                throw new IllegalStateException("Mật khẩu xác nhận không khớp.");
            }

            String confirmationText = console.readLine(
                    "Nhập RESET để xác nhận cập nhật tài khoản khang_admin: ");
            if (!"RESET".equals(confirmationText)) {
                throw new IllegalStateException("Chưa xác nhận; không thực hiện reset.");
            }

            resetService.resetPassword(new String(password));
            console.printf("Reset mật khẩu hoàn tất.%n");
        } finally {
            Arrays.fill(password, '\0');
            if (confirmation != null) {
                Arrays.fill(confirmation, '\0');
            }
        }
    }

    private static void validatePassword(char[] password) {
        if (password.length == 0) {
            throw new IllegalStateException("Mật khẩu không được để trống.");
        }
        if (new String(password).getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_BYTES) {
            throw new IllegalStateException("Mật khẩu vượt quá giới hạn của BCrypt.");
        }
    }
}
