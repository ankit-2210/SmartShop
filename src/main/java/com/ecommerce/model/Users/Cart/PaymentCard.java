package com.ecommerce.model.Users.Cart;

import com.ecommerce.model.Users.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class PaymentCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cardHolder;
    private String cardNumber;
    private String expiry;
    private String cardType;
    private boolean isDefault;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Return masked number for UI (e.g., •••• 4589)
    public String getCardNumberMasked() {
        if (cardNumber != null && cardNumber.length() >= 4) {
            return "•••• •••• •••• " + cardNumber.substring(cardNumber.length() - 4);
        }
        return cardNumber;
    }

}
