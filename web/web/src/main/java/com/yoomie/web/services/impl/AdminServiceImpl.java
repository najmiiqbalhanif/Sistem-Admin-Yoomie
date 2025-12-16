package com.yoomie.web.services.impl;

import com.yoomie.web.dto.AdminDTO;
import com.yoomie.web.models.Admin;
import com.yoomie.web.repositories.AdminRepository;
import com.yoomie.web.services.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AdminServiceImpl(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // helper: cek apakah string di DB terlihat seperti BCrypt hash
    private boolean isBcryptHash(String value) {
        if (value == null) return false;
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }

    @Override
    public Admin registerAdmin(AdminDTO adminDTO) {
        // cek email
        if (adminRepository.existsByEmail(adminDTO.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }
        // cek adminName
        if (adminRepository.existsByAdminName(adminDTO.getAdminName())) {
            throw new IllegalArgumentException("Admin name already in use");
        }

        Admin admin = new Admin();
        admin.setAdminName(adminDTO.getAdminName());
        admin.setEmail(adminDTO.getEmail());
        admin.setFullName(adminDTO.getFullName());

        // SIMPAN PASSWORD DALAM BENTUK HASH
        admin.setPassword(passwordEncoder.encode(adminDTO.getPassword()));

        return adminRepository.save(admin);
    }

    @Override
    public boolean authenticateAdmin(String email, String password) {
        return adminRepository.findByEmail(email)
                .map(admin -> {
                    String stored = admin.getPassword();

                    // 1) kalau sudah hash bcrypt → matches
                    if (isBcryptHash(stored)) {
                        return passwordEncoder.matches(password, stored);
                    }

                    // 2) kompatibilitas data lama (plaintext)
                    // kalau cocok, upgrade otomatis jadi bcrypt
                    boolean ok = stored != null && stored.equals(password);
                    if (ok) {
                        admin.setPassword(passwordEncoder.encode(password));
                        adminRepository.save(admin);
                    }
                    return ok;
                })
                .orElse(false);
    }

    @Override
    public Admin findByEmail(String email) {
        return adminRepository.findByEmail(email).orElse(null);
    }

    @Override
    public Admin getAdminById(Long id) {
        return adminRepository.findById(id).orElse(null);
    }

    @Override
    public void updateAdminProfile(Long id, AdminDTO dto) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Admin not found with id: " + id));

        admin.setFullName(dto.getFullName());
        admin.setAdminName(dto.getAdminName());
        admin.setEmail(dto.getEmail());

        // PENTING: jangan overwrite password dari profile update
        // Password ubahnya lewat changePassword()

        adminRepository.save(admin);
    }

    @Override
    public AdminDTO DTOgetAdminById(Long id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));
        return convertToDTO(admin);
    }

    private AdminDTO convertToDTO(Admin admin) {
        return AdminDTO.builder()
                .id(admin.getId())
                .adminName(admin.getAdminName())
                .email(admin.getEmail())
                // jangan pernah kirim password (meskipun hash) ke frontend
                .password(null)
                .fullName(admin.getFullName())
                .build();
    }

    @Override
    public void changePassword(Long id, String currentPassword, String newPassword) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Admin not found with id: " + id));

        String stored = admin.getPassword();
        boolean currentOk;

        if (isBcryptHash(stored)) {
            currentOk = passwordEncoder.matches(currentPassword, stored);
        } else {
            // kompatibilitas data lama (plaintext)
            currentOk = stored != null && stored.equals(currentPassword);
        }

        if (!currentOk) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }

        // set password baru dalam bentuk HASH
        admin.setPassword(passwordEncoder.encode(newPassword));
        adminRepository.save(admin);
    }
}
