package com.ecommerce.service;

import com.ecommerce.model.Users.Profile.Address;
import com.ecommerce.model.Users.Cart.PaymentCard;
import com.ecommerce.model.Users.Profile.User;

import java.util.List;

public interface UserService {

    public User saveUser(User user);

    public User getUserByEmail(String email);

    public List<Address> getAddressesByUser(User user);

    public Address getAddressById(Long id);

    public void deleteAddress(Long id);

    public List<User> getAllUsers(String role);

    public Boolean updateAccountStatus(Long id, Boolean status);

    public void increaseFailedAttempt(User user);

    public void userAccountLock(User user);

    public boolean unlockAccountTimeExpired(User user);

    public void resetAttempt(Long userId);

    public void updateUserResetToken(String email, String resetToken);

    public User getUserByToken(String token);

    public User updateUser(User user);

    public void makeDefault(Long userId, Long addressId);

    public Address updateAddress(Long userId, Long addressId, Address updateAddress);

    public PaymentCard updatePaymentCard(Long userId, Long cardId, PaymentCard updatedCard);

    public void makeDefaultCard(Long userId, Long cardId);

    public PaymentCard getCardById(Long id);

    public void deleteCard(Long id);

    public void deleteUser(Long userId);
}
