package com.kakaopay.spraying.domain.cache.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;

import static com.kakaopay.spraying.domain.cache.entity.CacheConst.*;

@Getter
@RedisHash(timeToLive = TTL)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Spray {
    @Id
    @NotNull
    private String token;

    @NotNull
    private String roomId;

    @NotNull
    private Integer userId;

    @Positive
    private long amount;

    @Positive
    private long amountUnit;

    @Positive
    private int numberOfDistributions;

    @NotNull
    private LocalDateTime createdAt;

    @NotNull
    private LocalDateTime expiredAt;

    @Builder
    public Spray(String token, int userId, String roomId,
                 int numberOfDistributions, long amountUnit, LocalDateTime current) {
        this.token = token;
        this.userId = userId;
        this.roomId = roomId;
        this.numberOfDistributions = numberOfDistributions;
        this.amountUnit = amountUnit;
        this.amount = numberOfDistributions * amountUnit;
        this.createdAt = current;
        this.expiredAt = current.plusSeconds(TTL);
    }

    public boolean isExpired() {
        return this.expiredAt.isBefore(LocalDateTime.now());
    }
}
