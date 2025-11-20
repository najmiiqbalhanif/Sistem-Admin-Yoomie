package com.yoomie.web.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDTO {
    private Long id;
    private String adminName;
    private String email;
    private String password;
    private String fullName;
}
