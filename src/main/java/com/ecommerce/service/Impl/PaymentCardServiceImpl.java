package com.ecommerce.service.Impl;

import com.ecommerce.model.Users.Cart.PaymentCard;
import com.ecommerce.model.Users.Profile.User;
import com.ecommerce.repository.PaymentCardRepository;
import com.ecommerce.service.PaymentCardService;
import com.paypal.api.payments.*;
import com.paypal.base.rest.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.*;
import java.util.*;

@Service
public class PaymentCardServiceImpl implements PaymentCardService {

    @Autowired
    private PaymentCardRepository paymentCardRepository;

    @Autowired
    private APIContext apiContext;

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


    public Payment createPayment(Double total, String currency, String method,
                                 String intent, String description,
                                 String cancelUrl, String successUrl) throws PayPalRESTException {

        // Round total to 2 decimal places
        BigDecimal rounded = BigDecimal.valueOf(total).setScale(2);

        // Create amount object
        Amount amount = new Amount();
        amount.setCurrency(currency);
        amount.setTotal(String.format("%.2f", rounded.doubleValue())); // PayPal expects string

        // Create transaction
        Transaction transaction = new Transaction();
        transaction.setDescription(description);
        transaction.setAmount(amount);

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        // Create payer
        Payer payer = new Payer();
        payer.setPaymentMethod(method.toLowerCase()); // e.g. "paypal"

        // Create payment object
        Payment payment = new Payment();
        payment.setIntent(intent); // e.g. "sale"
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        // Set redirect URLs
        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(cancelUrl);
        redirectUrls.setReturnUrl(successUrl);
        payment.setRedirectUrls(redirectUrls);

        // Create payment via PayPal API
        return payment.create(apiContext);  // ✅ use the injected APIContext
    }

    public Payment executePayment(String paymentId, String payerId) throws PayPalRESTException{
        Payment payment=new Payment();
        payment.setId(paymentId);
        PaymentExecution paymentExecution=new PaymentExecution();
        paymentExecution.setPayerId(payerId);
        return payment.execute(apiContext, paymentExecution);
    }

}
