package com.ecommerce.service.Impl;

import com.ecommerce.model.Users.Address;
import com.ecommerce.model.Users.Cart.PaymentCard;
import com.ecommerce.model.Users.User;
import com.ecommerce.repository.PaymentCardRepository;
import com.ecommerce.service.PaymentCardService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentCardServiceImpl implements PaymentCardService {

    @Autowired
    private PaymentCardRepository paymentCardRepository;

    @Transactional
    public PaymentCard saveCard(PaymentCard card, User user){
        card.setUser(user);

        if(card.isDefault()){
            List<PaymentCard> cards = paymentCardRepository.findByUserId(user.getId());
            for(PaymentCard c: cards){
                c.setDefault(false);
            }
            paymentCardRepository.saveAll(cards);
        }

        return paymentCardRepository.save(card);
    }

    @Override
    public List<PaymentCard> getPaymentCardByUser(User user) {
        return paymentCardRepository.findByUserId(user.getId());
    }


}
