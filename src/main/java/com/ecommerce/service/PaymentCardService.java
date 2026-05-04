package com.ecommerce.service;

import com.ecommerce.model.Users.Cart.PaymentCard;
import com.ecommerce.model.Users.Profile.User;

import java.util.List;

public interface PaymentCardService {
    public PaymentCard saveCard(PaymentCard card, User user);

    public List<PaymentCard> getPaymentCardByUser(User user);

}
