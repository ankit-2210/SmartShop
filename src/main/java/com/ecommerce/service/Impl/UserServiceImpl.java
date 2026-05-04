package com.ecommerce.service.Impl;

import com.ecommerce.model.Users.Profile.Address;
import com.ecommerce.model.Users.Cart.PaymentCard;
import com.ecommerce.model.Users.Profile.User;
import com.ecommerce.repository.AddressRepository;
import com.ecommerce.repository.PaymentCardRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.UserService;
import com.ecommerce.util.AppConstant;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private PaymentCardRepository paymentCardRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Override
    public User saveUser(User user){

        user.setRole("ROLE_USER");
        user.setEnable(true);
        user.setAccountNonLocked(true);
        user.setFailedAttempt(0);
        
        String encodePassword=passwordEncoder.encode(user.getPassword());
        user.setPassword(encodePassword);

        User savedUser = userRepository.save(user);

        // Create default address from user fields
        Address defaultAddress = new Address();
        defaultAddress.setTitle("Default");
        defaultAddress.setFullName(user.getUsername());
        defaultAddress.setPhone(user.getMobileNumber());
        defaultAddress.setStreet(user.getAddress());
        defaultAddress.setCity(user.getCity());
        defaultAddress.setState(user.getState());
        defaultAddress.setZip(user.getPincode());
        defaultAddress.setCountry(user.getCountry());
        defaultAddress.setIsDefault(true);
        defaultAddress.setUser(savedUser);

        addressRepository.save(defaultAddress);

        return savedUser;
    }

    @Override
    public User getUserByEmail(String email){
        return userRepository.getUserByEmail(email);
    }

    @Override
    public List<Address> getAddressesByUser(User user) {
        return addressRepository.findByUserId(user.getId());
    }

    @Override
    public Address getAddressById(Long id) {
        return addressRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteAddress(Long id) {
        addressRepository.deleteById(id);
    }


    @Transactional
    public void makeDefault(Long userId, Long addressId) {
        // 1. Remove default flag from all addresses of this user
        addressRepository.updateDefaultAddress(userId);

        // 2. Set the selected address as default
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (address.getUser().getId() != userId) {
            throw new RuntimeException("Unauthorized action");
        }

        address.setIsDefault(true);
        addressRepository.save(address);
    }

    @Transactional
    public Address updateAddress(Long userId, Long addressId, Address updateAddress){
        Address address = addressRepository.findById(addressId).orElseThrow(()-> new RuntimeException("Address not found"));

        if(!address.getUser().getId().equals(userId)){
            throw new RuntimeException("Unauthorized access");
        }

        address.setTitle(updateAddress.getTitle());
        address.setFullName(updateAddress.getFullName());
        address.setPhone(updateAddress.getPhone());
        address.setStreet(updateAddress.getStreet());
        address.setCity(updateAddress.getCity());
        address.setState(updateAddress.getState());
        address.setZip(updateAddress.getZip());
        address.setCountry(updateAddress.getCountry());
        address.setIsDefault(updateAddress.getIsDefault());

        if(updateAddress.getIsDefault()){
            addressRepository.makeAllOtherAddressesNonDefault(userId, addressId);
        }

        return addressRepository.save(address);
    }


    @Override
    public PaymentCard getCardById(Long id) {
        return paymentCardRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteCard(Long id) {
        paymentCardRepository.deleteById(id);
    }



    @Transactional
    public PaymentCard updatePaymentCard(Long userId, Long cardId, PaymentCard updatedCard) {
        // Fetch card by ID
        PaymentCard paymentCard = paymentCardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        // Ensure card belongs to this user
        if (!paymentCard.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access");
        }

        // Update fields
        paymentCard.setCardType(updatedCard.getCardType());
        paymentCard.setCardNumber(updatedCard.getCardNumber());
        paymentCard.setExpiry(updatedCard.getExpiry());
        paymentCard.setDefault(updatedCard.isDefault());

        // If this card is set as default, make all others non-default
        if (updatedCard.isDefault()) {
            paymentCardRepository.makeAllOtherCardsNonDefault(userId, cardId);
        }

        // Save and return updated card
        return paymentCardRepository.save(paymentCard);
    }

    @Transactional
    public void makeDefaultCard(Long userId, Long cardId) {
        // 1. Remove default flag from all addresses of this user
        paymentCardRepository.updateDefaultCard(userId);

        // 2. Set the selected address as default
        PaymentCard paymentCard = paymentCardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        if (paymentCard.getUser().getId() != userId) {
            throw new RuntimeException("Unauthorized action");
        }

        paymentCard.setDefault(true);
        paymentCardRepository.save(paymentCard);
    }



    @Override
    public List<User> getAllUsers(String role){
        return userRepository.findByRole(role);
    }

    @Override
    public Boolean updateAccountStatus(Long id, Boolean status){
        Optional<User> findByUser=userRepository.findById(id);
        if(findByUser.isPresent()){
            User user=findByUser.get();
            user.setEnable(status);

            userRepository.save(user);
            return true;
        }

        return false;
    }

    @Override
    public void increaseFailedAttempt(User user){
        int attempt=user.getFailedAttempt()+1;
        user.setFailedAttempt(attempt);

        userRepository.save(user);
    }

    @Override
    public void userAccountLock(User user){
        user.setAccountNonLocked(false);
        user.setLockTime(new Date());

        userRepository.save(user);
    }

    @Override
    public boolean unlockAccountTimeExpired(User user){
        long lockTime=user.getLockTime().getTime();
        long unLockTime=lockTime+ AppConstant.UNLOCK_DURATION_TIME;

        long currTime=System.currentTimeMillis();
        if(unLockTime < currTime){
            user.setAccountNonLocked(true);
            user.setFailedAttempt(0);
            user.setLockTime(null);

            userRepository.save(user);
            return true;
        }

        return false;
    }

    @Override
    public void resetAttempt(Long userId){


    }


    @Override
    public void updateUserResetToken(String email, String resetToken){
        User user=userRepository.getUserByEmail(email);
        user.setResetToken(resetToken);

        userRepository.save(user);
    }

    @Override
    public User getUserByToken(String token){
        return userRepository.getUserByResetToken(token);
    }


    @Override
    public User updateUser(User user){
        return userRepository.save(user);
    }


    @Override
    public void deleteUser(Long userId){
        if(!userRepository.existsById(userId)){
            throw new RuntimeException("User not found with ID: " + userId);
        }

        userRepository.deleteById(userId);
    }




}
