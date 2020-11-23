package com.kakaopay.spraying.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class SprayResponseException extends ResponseStatusException {
    private static final long serialVersionUID = 1L;

    private SprayResponseException(SprayErrors errorType) {
        super(errorType.getStatus(), errorType.getReason());
    }

    public static SprayResponseException of(SprayErrors errorType) {
        return new SprayResponseException(errorType);
    }

    @Getter
    @RequiredArgsConstructor
    public enum SprayErrors {
        NO_IDENTIFYING_INFORMATION(HttpStatus.UNAUTHORIZED, "식별 정보를 확인할 수 없습니다."),
        MINE_CANNOT_RECEIVE(HttpStatus.BAD_REQUEST, "자신이 뿌리기한 건은 자신이 받을 수 없습니다."),
        ONLY_BE_RECEIVED_ONCE(HttpStatus.BAD_REQUEST, "뿌리기 당 한 사용자는 한번만 받을 수 있습니다."),
        SPRAY_HISTORY_DOES_NOT_EXIST(HttpStatus.NOT_FOUND, "뿌리기 이력이 존재하지 않습니다."),
        ALL_EXHAUSTED(HttpStatus.BAD_REQUEST, "모두 소진되었습니다."),
        EXPIRED_OR_DOES_NOT_EXIST(HttpStatus.BAD_REQUEST, "만료되었거나 존재하지 않습니다."),
        ;

        private final HttpStatus status;
        private final String reason;
    }
}
