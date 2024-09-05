package com.FoodDeliveryWebApp.ServiceI;

import com.FoodDeliveryWebApp.Entity.User;
import com.FoodDeliveryWebApp.Exception.UserNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface UserService {

    String registerTemporaryUser(User user);

    String verifyOtpToRegister(String email, String otp);

    User loginUser(String username, String password) throws UserNotFoundException;

    User getUserById(Long userId);

    User updateUserDetails(Long userId, User user);

    @Transactional
    String deleteProfilePicture(Long userId);

    void deleteUser(Long userId);

    void createPasswordResetOtpForUser(User user, String otp);

    void changeUserPassword(User user, String newPassword);

    User getUserByEmail(String email) throws UserNotFoundException;

    String requestPasswordReset(String email);

    public List<User> getAllUsers();

}
