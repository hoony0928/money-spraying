package com.kakaopay.spraying.domain.entity;

import com.kakaopay.spraying.domain.entity.embedable.Money;
import com.kakaopay.spraying.domain.entity.embedable.Token;
import com.kakaopay.spraying.domain.entity.embedable.User;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.LinkedList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MoneySpray {
    @Id
    private Token token;

    @Embedded
    private User user;

    @Embedded
    private Money money;

    @NotNull
    private LocalDateTime createdAt;

    @NotNull
    private LocalDateTime expiredAt;

    @ToString.Exclude
    @OneToMany(mappedBy = "moneySpray", cascade = CascadeType.ALL)
    private final List<Receiver> receivers = new LinkedList<>();

    @Builder
    public MoneySpray(User user, Token token, Money money, LocalDateTime createdAt, Period days) {
        this.user = user;
        this.token = token;
        this.money = money;
        this.createdAt = createdAt;
        this.expiredAt = createdAt.plusDays(days.getDays());

    }

    public void add(Receiver receiver) {
        this.receivers.add(receiver);
    }

    public Long getAmount() {
        return money.getMoney();
    }
}