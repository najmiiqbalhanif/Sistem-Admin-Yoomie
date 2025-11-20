package com.yoomie.web.services;

import com.yoomie.web.dto.AdminDTO;
import com.yoomie.web.models.Admin;

public interface AdminService {

    Admin registerAdmin(AdminDTO adminDTO);

    boolean authenticateAdmin(String email, String password);

    Admin findByEmail(String email);

    Admin getAdminById(Long id);

    AdminDTO DTOgetAdminById(Long id);
}
