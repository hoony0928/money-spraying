package com.kakaopay.spraying.domain.cache.entity.repository.impl;

import com.kakaopay.spraying.domain.cache.entity.repository.SubtractCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import static com.kakaopay.spraying.domain.cache.entity.CacheConst.*;

@Repository
@RequiredArgsConstructor
public class SubtractCacheRepositoryImpl implements SubtractCacheRepository {
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Long subtract(String token) {
        return getHashOps(token).increment(DISTRIBUTIONS_FIELD, DISTRIBUTIONS_UNIT);
    }

    private BoundHashOperations<String, Object, Object> getHashOps(String key) {
        return redisTemplate.boundHashOps(SPRAY_KEY_SPACE + ":" + key);
    }
}