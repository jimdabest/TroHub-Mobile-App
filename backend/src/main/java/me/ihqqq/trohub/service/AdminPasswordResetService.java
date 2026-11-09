package me.ihqqq.trohub.service;

import lombok.RequiredArgsConstructor;
import me.ihqqq.trohub.entity.User;
import me.ihqqq.trohub.enums.Role;
import me.ihqqq.trohub.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminPasswordResetService {

    public static final String TARGET_USERNAME = "khang_admin";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void resetPassword(String newPassword) {
        User user = userRepository.findByUsername(TARGET_USERNAME)
                .orElseThrow(() -> new IllegalStateException("Tài khoản mục tiêu không tồn tại."));

        if (user.getRole() != Role.ADMIN) {
            throw new IllegalStateException("Tài khoản mục tiêu không có role ADMIN.");
        }

        String passwordHash = passwordEncoder.encode(newPassword);
        int updatedRows = userRepository.updatePasswordForRole(
                TARGET_USERNAME,
                Role.ADMIN,
                passwordHash);

        if (updatedRows != 1) {
            throw new IllegalStateException("Không thể xác nhận cập nhật chính xác một tài khoản.");
        }
    }
}
