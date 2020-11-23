package com.kakaopay.spraying.domain.repository;

import com.kakaopay.spraying.domain.entity.Receiver;
import com.kakaopay.spraying.domain.entity.embedable.Token;
import com.kakaopay.spraying.domain.entity.embedable.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceiverRepository extends JpaRepository<Receiver, Long> {
    boolean existsByUserAndMoneySpray_Token(User user, Token token);
}
