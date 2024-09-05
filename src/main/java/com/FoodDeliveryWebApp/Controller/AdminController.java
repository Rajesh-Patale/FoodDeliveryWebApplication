package com.FoodDeliveryWebApp.Controller;

import com.FoodDeliveryWebApp.Entity.Admin;
import com.FoodDeliveryWebApp.Exception.AdminNotFoundException;
import com.FoodDeliveryWebApp.Exception.UserNotFoundException;
import com.FoodDeliveryWebApp.ServiceI.AdminService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin("*")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private AdminService adminService;

    @PostMapping("/register")
    public ResponseEntity<String> registerAdmin(@RequestPart("adminData") String adminData,
                                                @RequestPart(value = "profilePicture", required = false) MultipartFile multipartFile) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Parse the admin data from JSON
            Admin admin = objectMapper.readValue(adminData, Admin.class);

            // Handle profile picture upload if provided
            if (multipartFile != null && !multipartFile.isEmpty()) {
                String contentType = multipartFile.getContentType();
                if (contentType != null && isValidImageType(contentType)) {
                    admin.setProfilePicture(multipartFile.getBytes());
                }else {
                    return ResponseEntity.badRequest().body("Invalid profile picture format. Only JPEG and PNG are supported.");
                }
            } else {
                admin.setProfilePicture(null);
            }

            // Register the admin
            String message = adminService.registerAdmin(admin);
            return ResponseEntity.status(HttpStatus.OK).body("Successfully registered Admin");
        } catch (JsonProcessingException e) {
            logger.error("Error parsing admin data: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid admin data format.");
        } catch (IllegalArgumentException e) {
            logger.error("Error during registration: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("An unexpected error occurred: {}", e.getMessage());
            return ResponseEntity.status(500).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    // Helper method to validate image types
    private  boolean isValidImageType(String contentType) {
        return !contentType.equalsIgnoreCase(".jpeg") &&
                !contentType.equalsIgnoreCase(".png") &&
                !contentType.equalsIgnoreCase(".jpg");
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginAdmin(@RequestParam String username, @RequestParam String password) {
        try {
            logger.info("Login attempt for username: {}", username);
            String result = adminService.loginAdmin(username, password);
            return ResponseEntity.status(HttpStatus.OK).body("Admin Login Successfully");
        } catch (RuntimeException e) {
            logger.error("Error occurred during login: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PutMapping("/update/{adminId}")
    public ResponseEntity<?> updateAdmin(
            @PathVariable Long adminId,
            @RequestPart("adminData") String adminData,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture) {

        try {
            // Convert JSON string to Admin object
            ObjectMapper objectMapper = new ObjectMapper();
            Admin adminDetails = objectMapper.readValue(adminData, Admin.class);

            // Update the admin
            Admin updatedAdmin = adminService.updateAdmin(adminId, adminDetails, profilePicture);

            // Return the updated admin
            return ResponseEntity.ok(updatedAdmin);

        } catch (AdminNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse admin data: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Invalid admin data format");
        } catch (Exception e) {
            logger.error("An unexpected error occurred: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{adminId}")
    public ResponseEntity<?> deleteAdmin(@PathVariable Long adminId) {
        try {
            if (adminId == null) {
                throw new UserNotFoundException("Admin id cannot be null");
            }
            adminService.deleteAdmin(adminId);
            return ResponseEntity.status(HttpStatus.OK).body("successfully deleted");
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Admin not found with id: " + adminId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }
}
