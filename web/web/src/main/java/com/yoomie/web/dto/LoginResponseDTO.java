package com.yoomie.web.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDTO {
    private Long id;
    private String cashierName;
    private String email;
    private String fullName;
    private String profileImage;
}