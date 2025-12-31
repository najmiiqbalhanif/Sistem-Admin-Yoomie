package com.yoomie.web.services;

import com.yoomie.web.dto.CashierDTO;
import com.yoomie.web.models.Cashier;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.io.IOException;

public interface CashierService {
    Cashier registerCashier(CashierDTO cashierDTO);

    Cashier registerCashierApp(CashierDTO cashierDTO);

    boolean authenticateCashier(String email, String password);

    Cashier findByEmail(String email);

    Cashier save(Cashier cashier);

    public Cashier getCashierById(Long id);

    public CashierDTO DTOgetCashierById(Long id);

    public void updateCashierProfile(Long cashierId, String cashierName, String email, String fullName, MultipartFile profileImage) throws IOException;

    void deactivateCashier(Long cashierId);

    void activateCashier(Long cashierId);

    List<CashierDTO> getAllActiveCashiers();

    List<CashierDTO> getAllCashiers();

    void changePassword(Long cashierId, String currentPassword, String newPassword);

}