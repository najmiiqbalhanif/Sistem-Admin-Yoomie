package com.yoomie.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {

    @NotBlank(message = "Current password wajib diisi.")
    private String currentPassword;

    @NotBlank(message = "New password wajib diisi.")
    @Size(min = 8, message = "Password baru minimal 8 karakter.")
    @Pattern(regexp = "^(?=.*[^A-Za-z0-9]).{8,}$",
            message = "Password baru harus mengandung minimal 1 karakter spesial (contoh: !@#$%).")
    private String newPassword;

    @NotBlank(message = "Confirm password wajib diisi.")
    private String confirmPassword;
}
