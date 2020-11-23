package com.kakaopay.spraying.domain.cache.entity.repository;

import com.kakaopay.spraying.domain.cache.entity.Spray;
import org.springframework.data.repository.CrudRepository;

public interface SprayCacheRepository extends CrudRepository<Spray, String>, SubtractCacheRepository {
}