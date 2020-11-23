package com.kakaopay.spraying.service.impl;

import com.kakaopay.spraying.configure.support.Identifier;
import com.kakaopay.spraying.domain.cache.entity.Spray;
import com.kakaopay.spraying.domain.cache.entity.repository.SprayCacheRepository;
import com.kakaopay.spraying.domain.entity.MoneySpray;
import com.kakaopay.spraying.domain.entity.embedable.Token;
import com.kakaopay.spraying.domain.repository.ReceiverRepository;
import com.kakaopay.spraying.domain.repository.SprayHistoryRepository;
import com.kakaopay.spraying.exception.SprayResponseException;
import com.kakaopay.spraying.exception.SprayResponseException.SprayErrors;
import com.kakaopay.spraying.service.SprayService;
import com.kakaopay.spraying.util.RandomTokenUtils;
import com.kakaopay.spraying.work.SprayWorkParam;
import com.kakaopay.spraying.work.WorkEvent;
import com.kakaopay.spraying.work.WorkEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Predicate;

import static com.kakaopay.spraying.api.ReceiveApiController.ReceiveResponseDto;
import static com.kakaopay.spraying.api.SprayApiController.SprayStateResponseDto;
import static com.kakaopay.spraying.api.SprayApiController.SprayResponseDto;
import static com.kakaopay.spraying.work.SprayWorkParam.Action;

@Service
@RequiredArgsConstructor
public class SprayServiceImpl implements SprayService {
    private final SprayCacheRepository sprayCacheRepository;
    private final SprayHistoryRepository sprayHistoryRepository;
    private final ReceiverRepository receiverRepository;
    private final WorkEventPublisher<SprayWorkParam> publisher;

    @Transactional
    @Override
    public SprayResponseDto spray(Identifier identifier, int numberOfDistributions,
                                  long amountUnit, LocalDateTime current) {

        final Spray spray = sprayCacheRepository.save(
                makeSpray(identifier, numberOfDistributions, amountUnit, current));

        publisher.publish(WorkEvent.of(this, SprayWorkParam.of(identifier, spray, Action.SPRAY)));
        return SprayResponseDto.of(spray);
    }

    @Transactional
    @Override
    public ReceiveResponseDto receive(Identifier identifier, Token token) {
        Spray spray = getSpray(identifier, token.getToken());
        if (isMine(identifier, spray)) {
            throw SprayResponseException.of(SprayErrors.MINE_CANNOT_RECEIVE);
        }

        if (isOverlap(identifier, token)) {
            throw SprayResponseException.of(SprayResponseException.SprayErrors.ONLY_BE_RECEIVED_ONCE);
        }

        if (isExhausted(token.getToken())) {
            throw SprayResponseException.of(SprayErrors.ALL_EXHAUSTED);
        }

        publisher.publish(WorkEvent.of(this, SprayWorkParam.of(identifier, spray, Action.RECEIVE)));

        return ReceiveResponseDto.of(spray);
    }

    @Override
    public SprayStateResponseDto getHistory(
            Token token, Identifier identifier, LocalDateTime current) {
        Optional<MoneySpray> sprayOptional = sprayHistoryRepository.findByTokenAndUserAndCreatedAtBetween(
                token, identifier.toEntity(), current.minusDays(7), current);

        return sprayOptional.map(SprayStateResponseDto::of)
                .orElseThrow(() -> SprayResponseException.of(SprayErrors.RECEIVED_HISTORY_DOES_NOT_EXIST));
    }

    private Spray makeSpray(
            Identifier identifier, int numberOfDistributions, long amountUnit, LocalDateTime current) {
        return Spray
                .builder()
                .token(RandomTokenUtils.turn())
                .userId(identifier.getUserId())
                .roomId(identifier.getRoomId())
                .numberOfDistributions(numberOfDistributions)
                .amountUnit(amountUnit)
                .current(current)
                .build();
    }

    private Spray getSpray(Identifier identifier, String token) {
        Optional<Spray> sprayingOptional = sprayCacheRepository.findById(token);

        return sprayingOptional
                .filter(roomMatcher(identifier))
                .filter(unExpiredMatcher())
                .orElseThrow(() -> SprayResponseException.of(SprayErrors.EXPIRED_OR_DOES_NOT_EXIST));
    }

    private Predicate<Spray> unExpiredMatcher() {
        return it -> !it.isExpired();
    }

    private Predicate<Spray> roomMatcher(Identifier identifier) {
        return it -> it.getRoomId().equals(identifier.getRoomId());
    }

    private boolean isMine(Identifier identifier, Spray spray) {
        return identifier.getUserId().equals(spray.getUserId());
    }

    private boolean isOverlap(Identifier identifier, Token token) {
        return receiverRepository.existsByUserAndMoneySpray_Token(
                identifier.toEntity(), token);
    }

    private boolean isExhausted(String token) {
        return 0 > sprayCacheRepository.subtract(token);
    }
}
