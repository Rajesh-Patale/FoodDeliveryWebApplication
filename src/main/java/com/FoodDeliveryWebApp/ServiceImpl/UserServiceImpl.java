package com.FoodDeliveryWebApp.ServiceImpl;

import com.FoodDeliveryWebApp.Entity.ForgotPasswordOtp;
import com.FoodDeliveryWebApp.Entity.TemporaryUser;
import com.FoodDeliveryWebApp.Entity.User;
import com.FoodDeliveryWebApp.Exception.UserNotFoundException;
import com.FoodDeliveryWebApp.Repository.ForgotPasswordOtpRepository;
import com.FoodDeliveryWebApp.Repository.TemporaryUserRepository;
import com.FoodDeliveryWebApp.Repository.UserRepository;
import com.FoodDeliveryWebApp.ServiceI.EmailService;
import com.FoodDeliveryWebApp.ServiceI.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import static com.FoodDeliveryWebApp.CommanUtil.ValidationClass.*;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private ForgotPasswordOtpRepository otpRepository;

    @Autowired
    private TemporaryUserRepository temporaryUserRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    private static final int OTP_EXPIRY_MINUTES = 5;

    private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);


    @Override
    @Transactional
    public String registerTemporaryUser(User user) {
        validateUserData(user);
        if (!user.getConfirmPassword().equals(user.getPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        String otp = generateOtp();

        TemporaryUser tempUser = new TemporaryUser();
        tempUser.setName(user.getName());
        tempUser.setEmail(user.getEmail());
        tempUser.setGender(user.getGender());
        tempUser.setMobileNo(user.getMobileNo());
        tempUser.setAddress(user.getAddress());
        tempUser.setUsername(user.getUsername());
        tempUser.setPassword(user.getPassword());
        tempUser.setConfirmPassword(user.getConfirmPassword());
        tempUser.setOtp(otp);
        tempUser.setOtpExpiry(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES)); // OTP expires in 5 minutes
        tempUser.setProfilePicture(user.getProfilePicture());
        temporaryUserRepository.save(tempUser);

        emailService.sendEmail(user.getEmail(), "Your OTP Code to register", "Your OTP code to register is: " + otp);

        return "Temporary user registered. Please verify OTP sent to your email.";
    }

    @Override
    @Transactional
    public String verifyOtpToRegister(String email, String otp) {
        logger.info("Attempting to verify OTP: {}", otp);
        Optional<TemporaryUser> tempUserOpt = temporaryUserRepository.findByOtp(otp);
        if (tempUserOpt.isEmpty()) {
            logger.error("OTP not found: {}", otp);
            throw new IllegalArgumentException("Temporary user not found");
        }

        TemporaryUser tempUser = tempUserOpt.get();
        logger.info("OTP found for email: {}", tempUser.getEmail());
        if (tempUser.getOtp().equals(otp) && tempUser.getOtpExpiry().isAfter(LocalDateTime.now())) {
            logger.info("OTP is valid and not expired");
            User user = new User();
            user.setName(tempUser.getName());
            user.setEmail(tempUser.getEmail());
            user.setGender(tempUser.getGender());
            user.setMobileNo(tempUser.getMobileNo());
            user.setAddress(tempUser.getAddress());
            user.setUsername(tempUser.getUsername());
            user.setPassword(tempUser.getPassword());
            user.setConfirmPassword(tempUser.getConfirmPassword());
            user.setProfilePicture(tempUser.getProfilePicture());
            user.setVerified(true);

            userRepository.save(user);
            temporaryUserRepository.delete(tempUser);

            return "User verified and registered successfully.";
        } else {
            return "Invalid or expired OTP.";
        }
    }

    @Override
    public User loginUser(String username, String password) throws UserNotFoundException {
        logger.info("Attempting to log in user with username: {}", username);

        // Retrieve the user by username and password
        return userRepository.findByUsernameAndPassword(username, password)
                .orElseThrow(() -> {
                    logger.warn("Login failed: User {} not found or password incorrect", username);
                    return new UserNotFoundException("Invalid username or password");
                });
    }

    @Override
    @Transactional
    public User updateUserDetails(Long userId, User user) {
        try {
            logger.info("Updating user by ID: {}", userId);
            // Retrieve the existing user or throw an exception if not found
            User existingUser = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        logger.warn("User with ID {} not found", userId);
                        return new UserNotFoundException("User with ID " + userId + " not found");
                    });

            // Update fields only if they are provided in the input user object
            if (user.getName() != null && !user.getName().isEmpty()) {
                existingUser.setName(user.getName());
            }
            if (user.getUsername() != null && !user.getUsername().isEmpty()) {
                existingUser.setUsername(user.getUsername());
            }
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                existingUser.setEmail(user.getEmail());
            }
            if (user.getGender() != null) {
                existingUser.setGender(user.getGender());
            }
            if (user.getMobileNo() != null && !user.getMobileNo().isEmpty()) {
                existingUser.setMobileNo(user.getMobileNo());
            }
            if (user.getAddress() != null && !user.getAddress().isEmpty()) {
                existingUser.setAddress(user.getAddress());
            }
            if (user.getProfilePicture() != null) {
                existingUser.setProfilePicture(user.getProfilePicture());
            }
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                existingUser.setPassword(user.getPassword());
            }
            if (user.getConfirmPassword()!= null &&!user.getConfirmPassword().isEmpty()) {
                existingUser.setConfirmPassword(user.getConfirmPassword());
            }
            logger.info("User details for ID {} have been updated: {}", userId, existingUser);

            // Save and return the updated user
            return userRepository.save(existingUser);

        } catch (UserNotFoundException e) {
            logger.error("Update failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred while updating user with ID {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to update user details", e);
        }
    }

    @Override
    public User getUserById(Long userId) {
        try {
            logger.info("Retrieving user with ID: {}", userId);
            // Retrieve the user by ID, or throw an exception if not found
            return userRepository.findById(userId)
                    .orElseThrow(() -> {
                        logger.warn("User with ID {} not found ", userId);
                        return new UserNotFoundException("User with ID " + userId + " not found");
                    });
        } catch (Exception e) {
            logger.error("An error occurred while retrieving user with ID: {}", userId, e);
            throw new RuntimeException("Failed to retrieve user", e);
        }
    }


    @Override
    @Transactional
    public String deleteProfilePicture(Long userId) {
        try {
            logger.info("Attempting to delete profile picture for user ID: {}", userId);
            // Retrieve the user by ID
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        logger.warn("User with ID {} not found :", userId);
                        return new UserNotFoundException("User with ID " + userId + " not found");
                    });
            // Remove the profile picture
            user.setProfilePicture(null);
            userRepository.save(user);

            logger.info("Profile picture deleted successfully for user ID: {}", userId);

            return "Profile picture deleted successfully.";

        } catch (UserNotFoundException e) {
            logger.error("Failed to delete profile picture: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred while deleting profile picture for user ID {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to delete profile picture", e);
        }
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        try {
            logger.info("Attempting to delete user with ID: {}", userId);
            // Retrieve the user by ID
            User existingUser = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        logger.warn("User with ID {} not found : ", userId);
                        return new UserNotFoundException("User with ID " + userId + " not found");
                    });
            // Delete the user
            userRepository.deleteById(userId);

            logger.info("Successfully deleted user with ID: {}", userId);

        } catch (UserNotFoundException e) {
            logger.error("Deletion failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred while deleting user with ID {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to delete user", e);
        }
    }

    @Override
    @Transactional
    public void createPasswordResetOtpForUser(User user, String otp) {
        Optional<ForgotPasswordOtp> existingOtp = otpRepository.findByUser(user);

        ForgotPasswordOtp otpEntity = existingOtp.orElseGet(ForgotPasswordOtp::new);
        otpEntity.setOtp(otp);
        otpEntity.setUser(user);
        otpEntity.setCreatedAt(LocalDateTime.now());
        otpEntity.setExpiryDate(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES)); // OTP expires in 5 minutes

        otpRepository.save(otpEntity);
    }

    private String generateOtp() {
        Random random = new Random();
        return String.valueOf(100000 + random.nextInt(900000)); // generate 6-digit OTP
    }

    @Override
    public void changeUserPassword(User user, String newPassword) {
        user.setPassword(newPassword);
        user.setConfirmPassword(newPassword);
        userRepository.save(user);
    }

    @Override
    public User getUserByEmail(String email) throws UserNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    @Override
    @Transactional
    public String requestPasswordReset(String email) {
        User user = getUserByEmail(email);
        String otp = generateOtp();
        createPasswordResetOtpForUser(user, otp);
        return otp;
    }

    private void validateUserData(User user) {
        if (user.getUsername() == null || !USERNAME_PATTERN.matcher(user.getUsername()).matches()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (user.getEmail() == null || !EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            throw new IllegalArgumentException("Email is not valid.");
        }
        if (user.getGender() == null || !GENDER_PATTERN.matcher(user.getGender()).matches()) {
            throw new IllegalArgumentException("Gender is required.");
        }
        if (user.getMobileNo() == null || !PHONE_PATTERN.matcher(user.getMobileNo()).matches()) {
            throw new IllegalArgumentException("Mobile number should be 10 digits.");
        }
        if (user.getAddress() == null || !ADDRESS_PATTERN.matcher(user.getAddress().toString()).matches()) {
            throw new IllegalArgumentException("Address is required.");
        }
        if (user.getPassword() == null || !PASSWORD_PATTERN.matcher(user.getPassword()).matches()) {
            throw new IllegalArgumentException("Password should be 6 characters.");
        }
        if (user.getConfirmPassword() == null || !PASSWORD_PATTERN.matcher(user.getConfirmPassword()).matches()) {
            throw new IllegalArgumentException("Confirm Password should be 6 characters.");
        }
    }
    @Override
    public List<User> getAllUsers() {

        return userRepository.findAll();
    }

}
