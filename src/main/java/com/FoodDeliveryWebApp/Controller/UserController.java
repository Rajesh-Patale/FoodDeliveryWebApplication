package com.FoodDeliveryWebApp.Controller;

import com.FoodDeliveryWebApp.Entity.ForgotPasswordOtp;
import com.FoodDeliveryWebApp.Entity.Message;
import com.FoodDeliveryWebApp.Entity.PasswordResetRequest;
import com.FoodDeliveryWebApp.Entity.User;
import com.FoodDeliveryWebApp.Exception.UserNotFoundException;
import com.FoodDeliveryWebApp.Repository.ForgotPasswordOtpRepository;
import com.FoodDeliveryWebApp.ServiceI.EmailService;
import com.FoodDeliveryWebApp.ServiceI.MessageService;
import com.FoodDeliveryWebApp.ServiceI.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ForgotPasswordOtpRepository otpRepository;

    @PostMapping("/user/register")
    public ResponseEntity<String> registerUser(@RequestPart("userData") String userData,
                                               @RequestPart("profilePicture") MultipartFile multipartFile) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        User user = objectMapper.readValue(userData, User.class);
        try {
            if (multipartFile != null && !multipartFile.isEmpty()) {
                String contentType = multipartFile.getContentType();
                if (contentType == null || isValidImageType(contentType)) {
                    return ResponseEntity.badRequest().body("Invalid profile picture format. Only JPEG and PNG and png are supported.");
                }
                user.setProfilePicture(multipartFile.getBytes());
            } else {
                user.setProfilePicture(null);
            }
            String message = userService.registerTemporaryUser(user);
            return ResponseEntity.ok(message);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @PostMapping("/user/verifyOtpToRegisterUser")
    public ResponseEntity<String> verifyUserOtpToRegisterUser(@RequestParam String email, @RequestParam String otp) {
        try {
            String message = userService.verifyOtpToRegister(email, otp);
            return ResponseEntity.ok(message);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An unexpected error occurred: " + e.getMessage());
        }
    }


    @PostMapping("/user/login/{username}/{password}")
    public ResponseEntity<?> loginUser(@PathVariable String username, @PathVariable String password) {
        try {
            User user = userService.loginUser(username, password);

            logger.info("User {} logged in successfully", username);

            return ResponseEntity.ok(user);

        } catch (UserNotFoundException e) {
            logger.warn("Login attempt failed for user {}: {}", username, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        } catch (Exception e) {
            logger.error("An unexpected error occurred during login for user {}: {}", username, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @PutMapping("/user/update/{userId}")
    public ResponseEntity<User> updateUser(@PathVariable Long userId,
                                           @RequestPart("userData") String userData,
                                           @RequestPart(value = "profilePicture", required = false) MultipartFile multipartFile) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            User user = objectMapper.readValue(userData, User.class);
            // If a profile picture is provided, validate and set it
            if (multipartFile != null && !multipartFile.isEmpty()) {
                String contentType = multipartFile.getContentType();
                if (contentType == null || isValidImageType(contentType)) {
                    return ResponseEntity.badRequest().body(null);
                }
                user.setProfilePicture(multipartFile.getBytes());
            } else {
                User existingUser = userService.getUserById(userId);
                // If no new profile picture is provided, keep the existing one
                user.setProfilePicture(existingUser.getProfilePicture());
            }
            // Update user details
            User updatedUser = userService.updateUserDetails(userId, user);
            return ResponseEntity.ok(updatedUser);

        } catch (JsonProcessingException e) {
            logger.error("Failed to parse user data: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            logger.error("An error occurred while updating user with ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/user/getUserById/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        try {
            if (userId == null) {
                logger.warn("User ID cannot be null");
                return ResponseEntity.badRequest().body("User ID cannot be null");
            }
            // Retrieve the user
            User user = userService.getUserById(userId);
            // Return the retrieved user
            return ResponseEntity.ok(user);

        } catch (UserNotFoundException e) {
            logger.warn("User not found with ID: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with ID: " + userId);
        } catch (Exception e) {
            logger.error("An unexpected error occurred while retrieving user with ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @DeleteMapping("/user/profilePicture/delete/{userId}")
    public ResponseEntity<String> deleteProfilePicture(@PathVariable Long userId) {
        try {
            logger.info("Received request to delete profile picture for user ID: {}", userId);
            // Call the service method to delete the profile picture
            String message = userService.deleteProfilePicture(userId);
            // Return a success response
            return ResponseEntity.ok(message);

        } catch (UserNotFoundException e) {
            logger.warn("Profile picture deletion failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("An unexpected error occurred: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }


    @DeleteMapping("/user/delete/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        try {
            logger.info("Received request to delete user with ID: {}", userId);
            // Check if the user ID is null
            if (userId == null) {
                logger.warn("User ID cannot be null ");
                throw new UserNotFoundException("User ID cannot be null");
            }
            // Call the service method to delete the user
            userService.deleteUser(userId);
            // Return a success response
            return ResponseEntity.ok("Successfully deleted user with ID: " + userId);

        } catch (UserNotFoundException e) {
            logger.warn("Deletion failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("An unexpected error occurred: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }


    @PostMapping("/user/verifyMail/{userEmail}")
    public ResponseEntity<String> forgotPassword(@PathVariable String userEmail) {
        logger.info("Received the request to reset password for email: {}", userEmail);
        try {
            if (userService.getUserByEmail(userEmail) == null) {
                return ResponseEntity.status(404).body("User not found with email: " + userEmail);
            }
            String otp = userService.requestPasswordReset(userEmail);
            emailService.sendEmail(userEmail, "Password Reset OTP","Your OTP for password reset is: " + otp);
            return ResponseEntity.ok("Password reset OTP has been sent to your email.");
        } catch (UserNotFoundException e) {
            logger.error("User not found with this email: {}", userEmail);
            return ResponseEntity.status(404).body("User not found with email: " + userEmail);
        } catch (Exception e) {
            logger.error("An error occurred while processing the password reset request for email: {}", userEmail, e);
            return ResponseEntity.status(500).body("An unexpected error occurred.");
        }
    }

    @PostMapping("/user/verifyForgotPasswordOtp")
    public ResponseEntity<String> validateForgotPasswordOtp(@RequestParam String otp, @RequestParam String userEmail) {
        logger.info("Received request to validate OTP: {} for email: {}", otp, userEmail);

        // Step 1: Check if the user exists
        try {
            User user = userService.getUserByEmail(userEmail);
        } catch (UserNotFoundException e) {
            logger.error("User not found with email: {}", userEmail, e);
            return ResponseEntity.status(404).body("User not found with email: " + userEmail);
        }
        // Step 2: Validate the OTP
        try {
            ForgotPasswordOtp passOtp = otpRepository.findByOtp(otp)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid OTP"));

            // Check if OTP has expired
            LocalDateTime now = LocalDateTime.now();
            if (passOtp.getCreatedAt().plusMinutes(5).isBefore(now)) { // Assuming 5 minutes expiration time
                otpRepository.delete(passOtp); // Optional: Clean up expired OTP
                logger.error("Expired OTP: {} for email: {}", otp, userEmail);
                return ResponseEntity.status(400).body("OTP has expired");
            }
            // OTP is valid
            logger.info("OTP is valid for email: {}", userEmail);
            return ResponseEntity.ok("OTP is verified, You can set new password ");

        } catch (IllegalArgumentException e) {
            logger.error("Invalid OTP: {} for email: {}", otp, userEmail, e);
            return ResponseEntity.status(400).body("Invalid or expired OTP");
        } catch (Exception e) {
            logger.error("An unexpected error occurred while validating OTP: {} for email: {}", otp, userEmail, e);
            return ResponseEntity.status(500).body("An unexpected error occurred.");
        }
    }


    @PostMapping("/user/resetPassword/{userEmail}")
    public ResponseEntity<String> resetPassword(@PathVariable("userEmail") String userEmail,
                                                @RequestBody PasswordResetRequest request) {
        String password = request.getPassword();
        String confirmPassword = request.getConfirmPassword();

        logger.info("Received request to reset password for email: {}", userEmail);
        // Step 1: Check if passwords match
        if (!password.equals(confirmPassword)) {
            logger.error("Passwords do not match for email: {}", userEmail);
            return ResponseEntity.status(400).body("Passwords do not match");
        }
        try {
            // Step 2: Retrieve the user by email
            User user = userService.getUserByEmail(userEmail);
            // Step 3: Change the user's password
            userService.changeUserPassword(user, password);
            return ResponseEntity.ok("Password successfully reset");

        } catch (UserNotFoundException e) {
            logger.error("User not found with email: {}", userEmail, e);
            return ResponseEntity.status(404).body("User not found with email: " + userEmail);
        } catch (Exception e) {
            logger.error("An error occurred while resetting password for email: {}", userEmail, e);
            return ResponseEntity.status(500).body("An unexpected error occurred.");
        }
    }

    private  boolean isValidImageType(String contentType) {
        return contentType.equalsIgnoreCase(".jpeg") ||
                contentType.equalsIgnoreCase(".png") ||
                contentType.equalsIgnoreCase(".jpg");
    }
    @GetMapping("/getalluserswithmsg")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    @GetMapping("/messages/{userId}")
    public ResponseEntity<List<Message>> getMessagesByUserId(@PathVariable Long userId) {
        List<Message> messages = messageService.getMessagesByUserId(userId);
        return ResponseEntity.ok(messages);
    }
}