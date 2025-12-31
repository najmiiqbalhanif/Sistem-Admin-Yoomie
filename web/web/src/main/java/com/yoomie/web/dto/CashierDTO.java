package com.yoomie.web.dto;

import lombok.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashierDTO {

    private Long id;

    @Size(min = 3, max = 20, message = "CashierName must be between 3 and 20 characters")
    private String cashierName;

    @Email(message = "Email is not valid")
    private String email;

    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(
            regexp = "^(?=.*[^A-Za-z0-9]).{8,}$",
            message = "Password must contain at least 1 special character (example: !@#$%)"
    )
    private String password;

    @Size(min = 3, max = 50, message = "Full name must be between 3 and 50 characters")
    private String fullName;

    private String profileImage;

    private Boolean active;
}
