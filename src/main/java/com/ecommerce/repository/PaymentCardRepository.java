package com.ecommerce.repository;

import com.ecommerce.model.Users.Cart.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long> {
    List<PaymentCard> findByUserId(Long userId);

    @Modifying
    @Query("UPDATE PaymentCard c SET c.isDefault = false WHERE c.user.id = :userId")
    void updateDefaultCard(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE PaymentCard c SET c.isDefault = false WHERE c.user.id = :userId AND c.id <> :cardId")
    void makeAllOtherCardsNonDefault(@Param("userId") Long userId, @Param("cardId") Long cardId);


}
