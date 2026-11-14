package com.uth.trohub.util

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Trạng thái hỗ trợ phần cứng sinh trắc học trên thiết bị
 */
sealed class BiometricStatus {
    object Ready : BiometricStatus()
    object NotAvailable : BiometricStatus()
    object NoneEnrolled : BiometricStatus()
    data class Error(val message: String) : BiometricStatus()
}

/**
 * Helper class quản lý xác thực Vân tay / Khuôn mặt (Biometric Authentication)
 * Sử dụng thư viện chính thức androidx.biometric.BiometricPrompt.
 */
object BiometricAuthHelper {

    /**
     * Kiểm tra thiết bị có hỗ trợ phần cứng và người dùng đã cài vân tay/khuôn mặt hay chưa.
     */
    fun checkBiometricAvailability(context: Context): BiometricStatus {
        val biometricManager = BiometricManager.from(context)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.BIOMETRIC_WEAK

        return when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.Ready
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricStatus.NotAvailable
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricStatus.Error("Cảm biến sinh trắc học hiện không khả dụng.")
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NoneEnrolled
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricStatus.Error("Cần cập nhật bảo mật để sử dụng sinh trắc học.")
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> BiometricStatus.Error("Không xác định được trạng thái sinh trắc học.")
            else -> BiometricStatus.Error("Thiết bị không hỗ trợ phương thức xác thực này.")
        }
    }

    /**
     * Hiển thị hộp thoại quét vân tay / nhận diện khuôn mặt.
     *
     * @param context Context của màn hình (tự động giải nén tìm FragmentActivity)
     * @param title Tiêu đề hiển thị (mặc định: "Đăng nhập Trọ Hub")
     * @param subtitle Phụ đề hướng dẫn
     * @param negativeButtonText Nhãn nút hủy / chuyển về mật khẩu
     * @param onSuccess Callback khi quét đúng vân tay / khuôn mặt
     * @param onFail Callback khi quét sai (nhưng chưa bị hủy)
     * @param onError Callback khi gặp lỗi (hủy quét, cảm biến lỗi, chưa đăng ký)
     */
    fun authenticate(
        context: Context,
        title: String = "Đăng nhập Trọ Hub",
        subtitle: String = "Xác thực vân tay hoặc khuôn mặt để vào ứng dụng nhanh chóng",
        negativeButtonText: String = "Dùng mật khẩu",
        onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        onFail: () -> Unit = {},
        onError: (errorCode: Int, errString: CharSequence) -> Unit
    ) {
        val activity = context.findFragmentActivity()
        if (activity == null) {
            onError(-1, "Activity hiện tại phải kế thừa FragmentActivity để hiển thị BiometricPrompt.")
            return
        }

        val executor = ContextCompat.getMainExecutor(context)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess(result)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onFail()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errorCode, errString)
            }
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .build()

        val biometricPrompt = BiometricPrompt(activity, executor, callback)
        biometricPrompt.authenticate(promptInfo)
    }

    /**
     * Tiện ích đệ quy tìm kiếm FragmentActivity từ LocalContext (Compose)
     */
    private fun Context.findFragmentActivity(): FragmentActivity? {
        var currentContext = this
        while (currentContext is ContextWrapper) {
            if (currentContext is FragmentActivity) {
                return currentContext
            }
            currentContext = currentContext.baseContext
        }
        return null
    }
}

