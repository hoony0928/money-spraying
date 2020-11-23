package com.kakaopay.spraying.configure;

import com.kakaopay.spraying.domain.cache.entity.CacheConst;
import com.kakaopay.spraying.domain.cache.entity.Spray;
import org.springframework.data.redis.core.convert.KeyspaceConfiguration;

import java.util.Collections;

public class SprayKeyspaceConfiguration extends KeyspaceConfiguration {
    @Override
    protected Iterable<KeyspaceSettings> initialConfiguration() {
        return Collections.singletonList(
                new KeyspaceSettings(Spray.class, CacheConst.SPRAY_KEY_SPACE));
    }
}