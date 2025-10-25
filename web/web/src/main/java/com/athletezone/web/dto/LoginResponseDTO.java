package com.athletezone.web.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDTO {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String profileImage;
}