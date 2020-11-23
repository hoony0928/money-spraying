package com.kakaopay.spraying.api;

import com.kakaopay.spraying.configure.support.Identifier;
import com.kakaopay.spraying.domain.cache.entity.Spray;
import com.kakaopay.spraying.domain.entity.MoneySpray;
import com.kakaopay.spraying.domain.entity.Receiver;
import com.kakaopay.spraying.domain.entity.embedable.Token;
import com.kakaopay.spraying.service.SprayService;
import com.kakaopay.spraying.util.ModelMapperUtils;
import io.swagger.annotations.*;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "뿌리기")
@RestController
@RequestMapping("api/spray")
@RequiredArgsConstructor
public class SprayApiController {
    private final SprayService sprayService;

    @ApiOperation(value = "뿌리기 요청")
    @PostMapping
    public ResponseEntity<SprayResponseDto> spraying(
            Identifier identifier,
            @RequestBody @Validated SprayingRequestDto requestDto) {
        return ResponseEntity.ok(
                sprayService.spray(identifier,
                        requestDto.getNumberOfDistributions(),
                        requestDto.getAmountUnit(),
                        LocalDateTime.now()));
    }

    @ApiOperation(value = "뿌리기 상태 조회 요청")
    @GetMapping
    public ResponseEntity<SprayStateResponseDto> getHistory(
            Identifier identifier,
            @ApiParam(value = "고유 식별 토큰", required = true) @RequestParam @Validated @Size(min = 3, max = 3) String token) {
        return ResponseEntity.ok(sprayService.getHistory(
                Token.of(token), identifier, LocalDateTime.now()));
    }

    @ApiModel("뿌리기 요청 모델")
    @Getter
    @Setter(AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static class SprayingRequestDto {
        @ApiModelProperty("요청금액")
        @Positive
        private long amountUnit;

        @ApiModelProperty("인원수")
        @Positive
        private int numberOfDistributions;
    }

    @ApiModel("뿌리기 요청 응답 모델")
    @Getter
    @Setter(AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SprayResponseDto {
        @ApiModelProperty("고유 식별 토큰")
        private String token;

        public static SprayResponseDto of(Spray spray) {
            return ModelMapperUtils.map(spray, SprayResponseDto.class);
        }
    }

    @ApiModel("뿌리기 상태 조회 요청 응답 모델")
    @Getter
    @Setter(AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SprayStateResponseDto {
        @ApiModelProperty("뿌린 시간")
        private LocalDateTime sprayingAt;
        @ApiModelProperty("뿌린 금액")
        private long amount;
        @ApiModelProperty("받기 완료된 금액")
        private long receivedAmount;

        @ApiModelProperty("받기 완료된 정보")
        private List<ReceivedSpray> receivers;

        public SprayStateResponseDto(
                LocalDateTime sprayingAt, long amount, List<ReceivedSpray> receivedSprays) {
            this.sprayingAt = sprayingAt;
            this.amount = amount;
            this.receivers = receivedSprays;
            this.receivedAmount = receivedSprays.stream()
                    .mapToLong(ReceivedSpray::getAmount)
                    .sum();
        }

        public static SprayStateResponseDto of(MoneySpray moneySpray) {
            final List<ReceivedSpray> receivedSprays = moneySpray.getReceivers().stream()
                    .map(ReceivedSpray::of)
                    .collect(Collectors.toList());

            return new SprayStateResponseDto(
                    moneySpray.getCreatedAt(), moneySpray.getAmount(), receivedSprays);
        }

        @ApiModel("받기 완료된 정보")
        @Getter
        @Setter(AccessLevel.PRIVATE)
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        @AllArgsConstructor(access = AccessLevel.PRIVATE)
        public static class ReceivedSpray {
            @ApiModelProperty("받은 금액")
            private long amount;
            @ApiModelProperty("받기 사용자 ID")
            private int userId;

            private static ReceivedSpray of(Receiver receiver) {
                return new ReceivedSpray(receiver.getMoney().getMoney(),
                        receiver.getUser().getUserId());
            }
        }
    }
}
