package com.athletezone.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentItemDTO {
    private String productName;
    private int quantity;
    private double price;
    private double subTotal;
}
