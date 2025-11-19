package com.yoomie.web.services.impl;

import com.yoomie.web.dto.CashierDTO;
import com.yoomie.web.models.Cashier;
import com.yoomie.web.models.Cart;
import com.yoomie.web.repositories.CartRepository;
import com.yoomie.web.repositories.CashierRepository;
import com.yoomie.web.services.CashierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
@Slf4j
public class CashierServiceImpl implements CashierService {

    private final CashierRepository cashierRepository;
    private final CartRepository cartRepository;

    private static final String UPLOAD_DIR = "uploads/";
    private static final String BASE_IMAGE_URL = "http://10.0.2.2:8080/";

    @Autowired
    public CashierServiceImpl(CashierRepository cashierRepository, CartRepository cartRepository) {
        this.cashierRepository = cashierRepository;
        this.cartRepository = cartRepository;
    }

    @Override
    public Cashier registerCashier(CashierDTO cashierDTO) {
        cashierRepository.findByEmail(cashierDTO.getEmail()).ifPresent(cashier -> {
            throw new IllegalStateException("Email already in use");
        });

        Cashier cashier = new Cashier();
        cashier.setCashierName(cashierDTO.getCashierName());
        cashier.setEmail(cashierDTO.getEmail());
        cashier.setPassword(cashierDTO.getPassword());

        cashier = cashierRepository.save(cashier);

        Cart cart = new Cart();
        cart.setCashier(cashier);
        cartRepository.save(cart);

        cashier.setCart(cart);

        return cashierRepository.save(cashier);
    }

    @Override
    @Transactional
    public Cashier registerCashierApp(CashierDTO cashierDTO) {
        cashierRepository.findByEmail(cashierDTO.getEmail()).ifPresent(cashier -> {
            throw new IllegalStateException("Email already in use");
        });

        try {
            Cashier cashier = new Cashier();
            cashier.setCashierName(cashierDTO.getCashierName());
            cashier.setEmail(cashierDTO.getEmail());
            cashier.setPassword(cashierDTO.getPassword());
            cashier.setFullName(cashierDTO.getFullName());

            // Simpan cashier dulu (supaya punya ID)
            cashier = cashierRepository.save(cashier);

            // Buat cart baru
            Cart cart = new Cart();
            cart.setTotalPrice(0.0);          // default aman
            cart.setCashier(cashier);         // set relasi

            // Simpan cart
            cart = cartRepository.save(cart);

            // Set balik ke cashier
            cashier.setCart(cart);

            // Simpan update cashier (opsional, tapi rapi)
            cashier = cashierRepository.save(cashier);

            return cashier;
        } catch (Exception e) {
            log.error("Error while registering cashier & creating cart", e);
            // biar @Transactional trigger rollback:
            throw e;
        }
    }

    @Override
    public boolean authenticateCashier(String email, String password) {
        return cashierRepository.findByEmail(email)
                .map(cashier -> cashier.getPassword().equals(password))
                .orElse(false);
    }

    @Override
    public Cashier findByEmail(String email) {
        return cashierRepository.findByEmail(email).orElse(null);
    }

    @Override
    public Cashier getCashierById(Long id) {
        return cashierRepository.findById(id).orElse(null);
    }

    @Override
    public CashierDTO DTOgetCashierById(Long id) {
        Cashier cashier = cashierRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cashier not found"));
        return convertToDTO(cashier);
    }

    private CashierDTO convertToDTO(Cashier cashier) {
        String profileImagePath = cashier.getProfileImage() != null ? cashier.getProfileImage().replace("\\", "/") : "";
        String fullProfileImageUrl = profileImagePath.isEmpty() ? "" : BASE_IMAGE_URL + "ProfImg/" + profileImagePath;

        return CashierDTO.builder()
                .id(cashier.getId())
                .cashierName(cashier.getCashierName())
                .email(cashier.getEmail())
                .password(cashier.getPassword())
                .fullName(cashier.getFullName())
                .profileImage(fullProfileImageUrl)
                .build();
    }

    @Override
    public void updateCashierProfile(Long cashierId, String cashierName, String email, String fullName, MultipartFile profileImage) throws IOException {
        Cashier cashier = cashierRepository.findById(cashierId)
                .orElseThrow(() -> new IllegalArgumentException("Cashier not found"));

        cashier.setCashierName(cashierName);
        cashier.setEmail(email);
        cashier.setFullName(fullName);

        if (profileImage != null && !profileImage.isEmpty()) {
            String relativePath = saveProfileImg(profileImage);
            cashier.setProfileImage(relativePath);
        }

        cashierRepository.save(cashier);
    }

    public String saveProfileImg(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return fileName;
    }
}
