package com.yoomie.web.controllerREST;

import com.yoomie.web.dto.CashierDTO;
import com.yoomie.web.services.CashierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profilepage")
@CrossOrigin(origins = "*") // Agar Flutter dapat mengakses (jika frontend dan backend terpisah)
public class profilePageControllerREST {

    private final CashierService cashierService;

    @Autowired
    public profilePageControllerREST(CashierService cashierService) {
        this.cashierService = cashierService;
    }

    @GetMapping("/{cashierId}")
    public ResponseEntity<CashierDTO> getProfileByCashierId(@PathVariable Long cashierId) {
        CashierDTO cashier = cashierService.DTOgetCashierById(cashierId);
        if (cashier == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(cashier);
    }
}
