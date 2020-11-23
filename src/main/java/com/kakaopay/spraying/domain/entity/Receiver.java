package com.kakaopay.spraying.domain.entity;

import com.kakaopay.spraying.domain.entity.embedable.Money;
import com.kakaopay.spraying.domain.entity.embedable.User;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Receiver {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long receiverId;

    @NotNull
    @Embedded
    private User user;

    @Embedded
    private Money money;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "token")
    private MoneySpray moneySpray;

    @Builder
    public Receiver(User user, Money money, MoneySpray moneySpray) {
        this.user = user;
        this.money = money;
        this.moneySpray = moneySpray;
    }
}