package com.yoomie.web.services.impl;

import com.yoomie.web.dto.CashierDTO;
import com.yoomie.web.models.Cashier;
import com.yoomie.web.models.Cart;
import com.yoomie.web.repositories.*;
import com.yoomie.web.services.CashierService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
                              PasswordEncoder passwordEncoder
    ) {
        this.cashierRepository = cashierRepository;
        this.cartRepository = cartRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Cashier registerCashier(CashierDTO cashierDTO) {
        cashierRepository.findByEmail(cashierDTO.getEmail()).ifPresent(cashier -> {
            throw new IllegalStateException("Email already in use");
        });

        Cashier cashier = new Cashier();
        cashier.setCashierName(cashierDTO.getCashierName());
        cashier.setEmail(cashierDTO.getEmail());
        cashier.setFullName(cashierDTO.getFullName()); // ✅ penting karena entity fullName non-null
        cashier.setPassword(passwordEncoder.encode(cashierDTO.getPassword())); // ✅ HASH

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
            cashier.setFullName(cashierDTO.getFullName());
            cashier.setPassword(passwordEncoder.encode(cashierDTO.getPassword())); // ✅ HASH

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
            throw e;
        }
    }

    @Override
    public boolean authenticateCashier(String email, String password) {
        return cashierRepository.findByEmailAndActiveTrue(email)
                .filter(Cashier::getActive)

                .map(cashier -> {
                    String stored = cashier.getPassword();

                    if (stored != null && stored.startsWith("$2")) {
                        return passwordEncoder.matches(password, stored);
                    }

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
    public List<CashierDTO> getAllActiveCashiers() {
        return cashierRepository.findAllByActiveTrue()
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
                .password(null) // ✅ JANGAN KIRIM HASH KE CLIENT
                .fullName(cashier.getFullName())
                .profileImage(fullProfileImageUrl)
                .build();
    }

    @Override
    public void updateCashierProfile(Long cashierId,
                                     String cashierName,
                                     String email,
                                     String fullName,
                                     MultipartFile profileImage) throws IOException {
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
                .orElseThrow(() ->
                        new EntityNotFoundException("Cashier not found: " + cashierId));

        if (Boolean.TRUE.equals(cashier.getDeleted())) {
            return; // sudah dihapus → tidak perlu apa-apa
        }

        cashier.setActive(false);
        cashier.setDeleted(true);

        cashier.setEmail(cashier.getEmail() + ".deleted." + cashier.getId());
    }



    // Change Password (Cashier)
    @Override
    @Transactional
    public void changePassword(Long cashierId, String currentPassword, String newPassword) {
        Cashier cashier = cashierRepository.findById(cashierId)
                .orElseThrow(() -> new IllegalArgumentException("Cashier not found"));

        String stored = cashier.getPassword();

        boolean currentOk;
        if (stored != null && stored.startsWith("$2")) {
            currentOk = passwordEncoder.matches(currentPassword, stored);
        } else {
            // legacy plain
            currentOk = stored != null && stored.equals(currentPassword);
            if (currentOk) {
                // upgrade legacy -> hash
                cashier.setPassword(passwordEncoder.encode(currentPassword));
                cashierRepository.save(cashier);
            }
        }

        if (!currentOk) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        cashier.setPassword(passwordEncoder.encode(newPassword));
        cashierRepository.save(cashier);
    }

    @Override
    public Cashier save(Cashier cashier) {
        return cashierRepository.save(cashier);
    }

}
