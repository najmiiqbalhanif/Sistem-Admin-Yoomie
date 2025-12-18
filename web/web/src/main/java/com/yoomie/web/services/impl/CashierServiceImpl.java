package com.yoomie.web.services.impl;

import com.yoomie.web.dto.CashierDTO;
import com.yoomie.web.models.Cashier;
import com.yoomie.web.models.Cart;
import com.yoomie.web.repositories.CartRepository;
import com.yoomie.web.repositories.CashierRepository;
import com.yoomie.web.services.CashierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CashierServiceImpl implements CashierService {

    private final CashierRepository cashierRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String UPLOAD_DIR = "uploads/";
    private static final String BASE_IMAGE_URL = "http://10.0.2.2:8080/";

    @Autowired
    public CashierServiceImpl(CashierRepository cashierRepository,
                              CartRepository cartRepository,
                              PasswordEncoder passwordEncoder) {
        this.cashierRepository = cashierRepository;
        this.cartRepository = cartRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private boolean isBcryptHash(String value) {
        if (value == null) return false;
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }

    @Override
    public Cashier registerCashier(CashierDTO cashierDTO) {
        if (cashierRepository.existsByEmail(cashierDTO.getEmail())) {
            throw new IllegalStateException("Email already in use");
        }
        if (cashierRepository.existsByCashierName(cashierDTO.getCashierName())) {
            throw new IllegalStateException("Cashier name already in use");
        }

        Cashier cashier = new Cashier();
        cashier.setCashierName(cashierDTO.getCashierName());
        cashier.setEmail(cashierDTO.getEmail());
        cashier.setFullName(cashierDTO.getFullName()); // penting karena entity fullName NOT NULL

        // ✅ HASH password
        cashier.setPassword(passwordEncoder.encode(cashierDTO.getPassword()));

        cashier = cashierRepository.save(cashier);

        Cart cart = new Cart();
        cart.setTotalPrice(0.0);
        cart.setCashier(cashier);
        cartRepository.save(cart);

        cashier.setCart(cart);
        return cashierRepository.save(cashier);
    }

    @Override
    @Transactional
    public Cashier registerCashierApp(CashierDTO cashierDTO) {
        if (cashierRepository.existsByEmail(cashierDTO.getEmail())) {
            throw new IllegalStateException("Email already in use");
        }
        if (cashierRepository.existsByCashierName(cashierDTO.getCashierName())) {
            throw new IllegalStateException("Cashier name already in use");
        }

        try {
            Cashier cashier = new Cashier();
            cashier.setCashierName(cashierDTO.getCashierName());
            cashier.setEmail(cashierDTO.getEmail());
            cashier.setFullName(cashierDTO.getFullName());

            // ✅ HASH password
            cashier.setPassword(passwordEncoder.encode(cashierDTO.getPassword()));

            // Simpan cashier dulu (supaya punya ID)
            cashier = cashierRepository.save(cashier);

            // Buat cart baru
            Cart cart = new Cart();
            cart.setTotalPrice(0.0);
            cart.setCashier(cashier);

            // Simpan cart
            cart = cartRepository.save(cart);

            // Set balik ke cashier
            cashier.setCart(cart);

            // Simpan update cashier (opsional)
            cashier = cashierRepository.save(cashier);

            return cashier;
        } catch (Exception e) {
            log.error("Error while registering cashier & creating cart", e);
            throw e; // trigger rollback
        }
    }

    @Override
    public boolean authenticateCashier(String email, String password) {
        return cashierRepository.findByEmail(email)
                .map(cashier -> {
                    String stored = cashier.getPassword();

                    // ✅ normal: bcrypt
                    if (isBcryptHash(stored)) {
                        return passwordEncoder.matches(password, stored);
                    }

                    // ✅ kompatibilitas data lama plaintext (optional)
                    boolean ok = stored != null && stored.equals(password);
                    if (ok) {
                        cashier.setPassword(passwordEncoder.encode(password));
                        cashierRepository.save(cashier);
                    }
                    return ok;
                })
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
    public List<CashierDTO> getAllCashiers() {
        return cashierRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
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
                .password(null) // ✅ jangan kirim password (meskipun hash) ke client
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

    @Override
    @Transactional
    public void deleteCashierById(Long cashierId) {
        Cashier cashier = cashierRepository.findById(cashierId)
                .orElseThrow(() -> new IllegalArgumentException("Cashier not found"));

        cashierRepository.delete(cashier);
    }
}
