package com.kakaopay.spraying.service;

import com.kakaopay.spraying.configure.support.Identifier;
import com.kakaopay.spraying.domain.entity.embedable.Token;

import java.time.LocalDateTime;

import static com.kakaopay.spraying.api.ReceiveApiController.ReceiveResponseDto;
import static com.kakaopay.spraying.api.SprayApiController.SprayStateResponseDto;
import static com.kakaopay.spraying.api.SprayApiController.SprayResponseDto;

public interface SprayService {
    SprayResponseDto spray(Identifier identifier, int number, long price, LocalDateTime current);
    ReceiveResponseDto receive(Identifier identifier, Token token);
    SprayStateResponseDto getHistory(
            Token token, Identifier identifier, LocalDateTime current);
}
