package com.kakaopay.spraying.api;

import com.kakaopay.spraying.configure.support.Identifier;
import com.kakaopay.spraying.domain.cache.entity.Spray;
import com.kakaopay.spraying.domain.entity.embedable.Token;
import com.kakaopay.spraying.service.SprayService;
import com.kakaopay.spraying.util.ModelMapperUtils;
import io.swagger.annotations.*;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Size;

@Api(tags = "받기")
@RestController
@RequestMapping("api/spray/receive")
@RequiredArgsConstructor
public class ReceiveApiController {
    private final SprayService sprayService;

    @ApiOperation(value = "뿌린 건 받기 요청")
    @PatchMapping
    public ResponseEntity<ReceiveResponseDto> receive(
            Identifier identifier,
            @ApiParam(value = "고유 식별 토큰", required = true) @RequestParam @Validated @Size(min = 3, max = 3) String token) {
        return ResponseEntity.ok(
                sprayService.receive(identifier, Token.of(token)));
    }

    @ApiModel("받기 요청 응답 모델")
    @Getter
    @Setter(AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ReceiveResponseDto {
        @ApiModelProperty("받은 금액")
        private long amountUnit;

        public static ReceiveResponseDto of(Spray spray) {
            return ModelMapperUtils.map(spray, ReceiveResponseDto.class);
        }
    }
}