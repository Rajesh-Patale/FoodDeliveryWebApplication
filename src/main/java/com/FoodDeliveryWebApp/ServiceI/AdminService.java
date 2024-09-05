package com.FoodDeliveryWebApp.ServiceI;

import com.FoodDeliveryWebApp.Entity.Admin;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

public interface AdminService {

    String registerAdmin(Admin admin);
    String loginAdmin(String username, String password);

    @Transactional
    Admin updateAdmin(Long adminId, Admin adminDetails, MultipartFile profilePicture);

    void deleteAdmin(Long adminId);
}