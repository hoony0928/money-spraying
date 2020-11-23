package com.kakaopay.spraying.domain.repository;

import com.kakaopay.spraying.domain.entity.MoneySpray;
import com.kakaopay.spraying.domain.entity.embedable.Token;
import com.kakaopay.spraying.domain.entity.embedable.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SprayHistoryRepository extends JpaRepository<MoneySpray, Token> {
    @EntityGraph(attributePaths = "receivers")
    Optional<MoneySpray> findByTokenAndUserAndCreatedAtBetween(Token token, User user, LocalDateTime fromAt, LocalDateTime toAt);
}
