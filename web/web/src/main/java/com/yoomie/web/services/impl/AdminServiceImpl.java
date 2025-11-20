    package com.yoomie.web.services.impl;

    import com.yoomie.web.dto.AdminDTO;
    import com.yoomie.web.models.Admin;
    import com.yoomie.web.repositories.AdminRepository;
    import com.yoomie.web.services.AdminService;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Service;

    @Service
    @Slf4j
    public class AdminServiceImpl implements AdminService {

        private final AdminRepository adminRepository;

        @Autowired
        public AdminServiceImpl(AdminRepository adminRepository) {
            this.adminRepository = adminRepository;
        }

        @Override
        public Admin registerAdmin(AdminDTO adminDTO) {
            // Cek email sudah dipakai atau belum
            adminRepository.findByEmail(adminDTO.getEmail()).ifPresent(a -> {
                throw new IllegalStateException("Email already in use");
            });

            Admin admin = new Admin();
            admin.setAdminName(adminDTO.getAdminName());
            admin.setEmail(adminDTO.getEmail());
            admin.setPassword(adminDTO.getPassword());
            admin.setFullName(adminDTO.getFullName());

            return adminRepository.save(admin);
        }

        @Override
        public boolean authenticateAdmin(String email, String password) {
            return adminRepository.findByEmail(email)
                    .map(admin -> admin.getPassword().equals(password))
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
                    .password(admin.getPassword())
                    .fullName(admin.getFullName())
                    .build();
        }
    }
