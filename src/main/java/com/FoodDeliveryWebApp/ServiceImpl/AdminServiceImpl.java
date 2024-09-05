package com.FoodDeliveryWebApp.ServiceImpl;

import com.FoodDeliveryWebApp.Entity.Admin;
import com.FoodDeliveryWebApp.Exception.AdminNotFoundException;
import com.FoodDeliveryWebApp.Repository.AdminRepository;
import com.FoodDeliveryWebApp.ServiceI.AdminService;
import com.FoodDeliveryWebApp.CommanUtil.ValidationClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.regex.Pattern;

@Service
public class AdminServiceImpl implements AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminServiceImpl.class);

    @Autowired
    private AdminRepository adminRepository;

    @Override
    @Transactional
    public String registerAdmin(Admin admin) {
        logger.info("Registering admin with username: {}", admin.getUsername());

        validateAdminData(admin);

        // Check if the username or email already exists
        if (adminRepository.existsByUsername(admin.getUsername()) || adminRepository.existsByEmail(admin.getEmail()) || adminRepository.existsByMobileNo(admin.getMobileNo())) {
            logger.error("Admin with username {} or email {} or mobile number {} already exists", admin.getUsername(), admin.getEmail(), admin.getMobileNo());
            throw new RuntimeException("Admin with this username or email or mobile number already exists");
        }

        // Handle profile picture
        if (admin.getProfilePicture() != null && admin.getProfilePicture().length > 0) {
            logger.info("Profile picture uploaded for admin with username: {}", admin.getUsername());
            admin.setProfilePicture(admin.getProfilePicture());
        } else {
            logger.info("No profile picture provided for admin with username: {}", admin.getUsername());
            admin.setProfilePicture(null);
        }
        // Save the admin entity
        Admin savedAdmin = adminRepository.save(admin);
        logger.info("Successfully registered admin with ID: {}", savedAdmin.getId());
        // Return a success message
        return "Admin registered successfully.";
    }

    @Override
    public String loginAdmin(String username, String password) {
        logger.info("Admin login attempt for username: {}", username);

        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("Admin not found with username: {}", username);
                    return new RuntimeException("Admin not found");
                });

        // Direct password comparison without encoding
        if (password.equals(admin.getPassword())) {
            logger.info("Login successful for username: {}", username);
            return "Login successful!";
        } else {
            logger.error("Invalid credentials for username: {}", username);
            throw new RuntimeException("Invalid credentials");
        }
    }

    @Override
    @Transactional
    public Admin updateAdmin(Long adminId, Admin adminDetails, MultipartFile profilePicture) {
        logger.info("Updating admin by id: {}, data: {}", adminId, adminDetails);

        // Retrieve the existing admin
        Admin existingAdmin = adminRepository.findById(adminId)
                .orElseThrow(() -> new AdminNotFoundException("Admin with ID " + adminId + " not found"));

        // Update fields only if the new value is not null and valid
        if (adminDetails.getName() != null && ValidationClass.NAME_PATTERN.matcher(adminDetails.getName()).matches()) {
            existingAdmin.setName(adminDetails.getName());
        }
        if (adminDetails.getMobileNo() != null && ValidationClass.PHONE_PATTERN.matcher(adminDetails.getMobileNo()).matches()) {
            existingAdmin.setMobileNo(adminDetails.getMobileNo());
        }
        if (adminDetails.getUsername() != null && ValidationClass.USERNAME_PATTERN.matcher(adminDetails.getUsername()).matches()) {
            existingAdmin.setUsername(adminDetails.getUsername());
        }
        if (adminDetails.getPassword() != null && ValidationClass.PASSWORD_PATTERN.matcher(adminDetails.getPassword()).matches()) {
            existingAdmin.setPassword(adminDetails.getPassword());
        }
        if (adminDetails.getEmail() != null && ValidationClass.EMAIL_PATTERN.matcher(adminDetails.getEmail()).matches()) {
            existingAdmin.setEmail(adminDetails.getEmail());
        }

        // Update profile picture if a new one is provided and valid
        if (profilePicture != null && !profilePicture.isEmpty()) {
            String contentType = profilePicture.getContentType();
            if (contentType != null && (contentType.equals("image/jpeg") || contentType.equals("image/png"))) {
                try {
                    existingAdmin.setProfilePicture(profilePicture.getBytes());
                } catch (IOException e) {
                    logger.error("Failed to update profile picture for admin with id: {}", adminId, e);
                    throw new RuntimeException("Failed to update profile picture", e);
                }
            } else {
                logger.warn("Invalid file type: {}", contentType);
                throw new RuntimeException("Only JPEG or PNG images are allowed");
            }
        }
        // Save the updated admin
        Admin updatedAdmin = adminRepository.save(existingAdmin);
        logger.info("Successfully updated admin with id: {}", adminId);
        return updatedAdmin;
    }

    @Override
    public void deleteAdmin(Long adminId) {
        logger.info("Deleting Admin by id: {}", adminId);
        Admin existingAdmin = adminRepository.findById(adminId)
                .orElseThrow(() -> new AdminNotFoundException("Admin with ID " + adminId + " not found"));

        adminRepository.deleteById(adminId);
        logger.info("Successfully deleted admin with id: {}", adminId);
    }

    private void validateAdminData(Admin admin) {
        // Validate admin data
        if (admin.getUsername() == null || !ValidationClass.USERNAME_PATTERN.matcher(admin.getUsername()).matches()) {
            throw new IllegalArgumentException("Invalid username");
        }
        if (admin.getEmail() == null || !ValidationClass.EMAIL_PATTERN.matcher(admin.getEmail()).matches()) {
            throw new IllegalArgumentException("Invalid email");
        }
        if (admin.getPassword() == null || !ValidationClass.PASSWORD_PATTERN.matcher(admin.getPassword()).matches()) {
            throw new IllegalArgumentException("Invalid password");
        }
        if (admin.getMobileNo() == null || !ValidationClass.PHONE_PATTERN.matcher(admin.getMobileNo()).matches()) {
            throw new IllegalArgumentException("Invalid mobile number");
        }
    }
}
