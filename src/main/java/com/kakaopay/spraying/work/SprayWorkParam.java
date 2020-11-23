package com.kakaopay.spraying.work;

import com.kakaopay.spraying.configure.support.Identifier;
import com.kakaopay.spraying.domain.cache.entity.Spray;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor(staticName = "of")
public class SprayWorkParam {
    private final Identifier identifier;
    private final Spray spray;
    private final Action action;

    @RequiredArgsConstructor
    public enum Action {
        SPRAY, RECEIVE
    }
}
