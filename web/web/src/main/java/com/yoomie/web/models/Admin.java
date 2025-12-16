package com.yoomie.web.models;

import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "admins")
public class Admin extends BaseEntity {

    @NotBlank(message = "Admin Name wajib diisi.")
    private String adminName;

    @NotBlank(message = "Full Name wajib diisi.")
    private String fullName;

    @NotBlank(message = "Email wajib diisi.")
    @Email(message = "Format email tidak valid.")
    private String email;

    @NotBlank(message = "Password wajib diisi.")
    @Size(min = 8, message = "Password minimal 8 karakter.")
    @Pattern(
            regexp = "^(?=.*[^A-Za-z0-9]).{8,}$",
            message = "Password harus mengandung minimal 1 karakter spesial (contoh: !@#$%)."
    )
    private String password;

}
