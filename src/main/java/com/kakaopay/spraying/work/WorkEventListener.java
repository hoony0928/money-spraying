package com.kakaopay.spraying.work;

import com.kakaopay.spraying.configure.support.Identifier;
import com.kakaopay.spraying.domain.cache.entity.Spray;
import com.kakaopay.spraying.domain.entity.MoneySpray;
import com.kakaopay.spraying.domain.entity.Receiver;
import com.kakaopay.spraying.domain.entity.embedable.Money;
import com.kakaopay.spraying.domain.entity.embedable.Token;
import com.kakaopay.spraying.domain.entity.embedable.User;
import com.kakaopay.spraying.domain.repository.SprayHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Period;

import static com.kakaopay.spraying.work.SprayWorkParam.*;

@Component
@RequiredArgsConstructor
public class WorkEventListener {
    private final SprayHistoryRepository sprayHistoryRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, classes = WorkEvent.class)
    public void subscribe(WorkEvent<SprayWorkParam> event) {
        final SprayWorkParam param = event.getWhat();
        final MoneySpray moneySpray = sprayHistoryRepository.findById(Token.of(param.getSpray().getToken()))
                .orElse(getMoneySpray(param.getSpray()));

        if (Action.SPRAY == param.getAction()) {
            sprayHistoryRepository.save(moneySpray);
            return;
        }

        moneySpray.add(getReceiver(param.getIdentifier(), param.getSpray(), moneySpray));
        sprayHistoryRepository.save(moneySpray);
    }

    private MoneySpray getMoneySpray(Spray spray) {
        return MoneySpray.builder()
                .user(User.of(spray.getUserId(), spray.getRoomId()))
                .token(Token.of(spray.getToken()))
                .money(Money.of(spray.getAmount()))
                .createdAt(spray.getCreatedAt())
                .days(Period.ofDays(10))
                .build();
    }

    private Receiver getReceiver(Identifier identifier, Spray spray, MoneySpray moneySpray) {
        return Receiver.builder()
                .user(identifier.toEntity())
                .money(Money.of(spray.getAmountUnit()))
                .moneySpray(moneySpray)
                .build();
    }
}
