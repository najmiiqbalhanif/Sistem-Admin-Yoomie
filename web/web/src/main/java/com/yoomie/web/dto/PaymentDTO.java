package com.yoomie.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    private Long cashierId;
    private String cashierName;
    private String paymentMethod;
    private String address;
    private Double totalAmount;
}
