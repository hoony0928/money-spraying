package com.kakaopay.spraying.service;

import com.kakaopay.spraying.configure.support.Identifier;
import com.kakaopay.spraying.domain.cache.entity.CacheConst;
import com.kakaopay.spraying.domain.cache.entity.Spray;
import com.kakaopay.spraying.domain.cache.entity.repository.SprayCacheRepository;
import com.kakaopay.spraying.domain.entity.embedable.Token;
import com.kakaopay.spraying.domain.repository.ReceiverRepository;
import com.kakaopay.spraying.domain.repository.SprayHistoryRepository;
import com.kakaopay.spraying.exception.SprayResponseException;
import com.kakaopay.spraying.service.impl.SprayServiceImpl;
import com.kakaopay.spraying.work.SprayWorkParam;
import com.kakaopay.spraying.work.WorkEventPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.stream.Stream;

import static com.kakaopay.spraying.api.ReceiveApiController.ReceiveResponseDto;
import static com.kakaopay.spraying.api.SprayApiController.SprayResponseDto;
import static com.kakaopay.spraying.exception.SprayResponseException.SprayErrors;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SprayServiceTest {
    @Mock
    private SprayCacheRepository sprayCacheRepository;
    @Mock
    private SprayHistoryRepository sprayHistoryRepository;
    @Mock
    private ReceiverRepository receiverRepository;
    @Mock
    private WorkEventPublisher<SprayWorkParam> publisher;
    @Captor
    private ArgumentCaptor<Spray> sprayCaptor;
    @InjectMocks
    private SprayServiceImpl sprayingService;

    static Stream<Arguments> providedForSpray() {
        return Stream.of(Arguments.of(Identifier.of(1, "room1"),
                LocalDateTime.of(LocalDate.of(2020, 11, 15),
                        LocalTime.of(12, 30, 15, 15))));
    }

    static Stream<Arguments> providedForReceive() {
        return Stream.of(Arguments.of(
                Identifier.of(1, "room1"),
                Identifier.of(2, "room1"),
                Token.of("abc"),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.of(LocalDate.of(2020, 11, 15),
                        LocalTime.of(12, 30, 15, 15))));
    }

    @DisplayName("돈 뿌리기 테스트")
    @ParameterizedTest
    @MethodSource("providedForSpray")
    void moneySprayTest(Identifier identifier, LocalDateTime current) {
        when(sprayCacheRepository.save(sprayCaptor.capture()))
                .thenAnswer((Answer<Spray>) invocation -> invocation.getArgument(0));

        SprayResponseDto expectedResponse = sprayingService.spray(
                identifier, 5, 10000, current);

        Spray expectedSpray = sprayCaptor.getValue();

        verify(publisher, times(1)).publish(any());

        assertNotNull(expectedSpray);
        assertEquals(3, expectedSpray.getToken().length());
        assertEquals(expectedSpray.getUserId(), 1);
        assertEquals(expectedSpray.getRoomId(), "room1");
        assertEquals(expectedSpray.getAmountUnit(), 10000);
        assertEquals(expectedSpray.getNumberOfDistributions(), 5);
        assertEquals(expectedSpray.getAmount(), 5 * 10000);
        assertEquals(expectedSpray.getCreatedAt(), current);
        assertEquals(expectedSpray.getExpiredAt(), current.plusSeconds(CacheConst.TTL));

        assertEquals(3, expectedResponse.getToken().length());
    }

    @DisplayName("받기 및 만료시간 테스트")
    @ParameterizedTest
    @MethodSource("providedForReceive")
    void receiveExpirationTest(Identifier idAsSpray,
                               Identifier idAsReceiver,
                               Token token,
                               LocalDateTime current,
                               LocalDateTime currentAsExpiration) {

        when(sprayCacheRepository.findById(eq(token.getToken())))
                .thenReturn(Optional.of(makeSpray(idAsSpray, current)))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(makeSpray(idAsSpray, currentAsExpiration)));

        when(sprayCacheRepository.subtract(eq(token.getToken())))
                .thenReturn(4L)
                .thenReturn(3L)
                .thenReturn(2L)
                .thenReturn(1L)
                .thenReturn(0L);

        when(receiverRepository.existsByUserAndMoneySpray_Token(any(), any()))
                .thenReturn(false);

        success(idAsReceiver, token);
        throwWhenExpiration(idAsReceiver, token);
        throwWhenExpiration(idAsReceiver, token);
    }

    @DisplayName("자신이 뿌린 돈은 받기 불가 테스트")
    @ParameterizedTest
    @MethodSource("providedForReceive")
    void mineCannotReceiveTest(Identifier idAsSpray,
                               Identifier idAsReceiver,
                               Token token,
                               LocalDateTime current) {

        when(sprayCacheRepository.findById(eq(token.getToken())))
                .thenReturn(Optional.of(makeSpray(idAsSpray, current)));

        throwWhenMine(idAsSpray, token);
    }

    @DisplayName("중복 받기 불가 테스트")
    @ParameterizedTest
    @MethodSource("providedForReceive")
    void onlyBeReceivedOnceTest(Identifier idAsSpray,
                                Identifier idAsReceiver,
                                Token token,
                                LocalDateTime current) {

        when(sprayCacheRepository.findById(eq(token.getToken())))
                .thenReturn(Optional.of(makeSpray(idAsSpray, current)));

        when(receiverRepository.existsByUserAndMoneySpray_Token(any(), any()))
                .thenReturn(true);

        throwWhenOverlap(idAsReceiver, token);
    }

    @DisplayName("뿌린 돈 소진 테스트")
    @ParameterizedTest
    @MethodSource("providedForReceive")
    void allExhaustedSprayTest(Identifier idAsSpray,
                               Identifier idAsReceiver,
                               Token token,
                               LocalDateTime current) {

        when(sprayCacheRepository.findById(eq(token.getToken())))
                .thenReturn(Optional.of(makeSpray(idAsSpray, current)));

        when(sprayCacheRepository.subtract(eq(token.getToken())))
                .thenReturn(-1L);

        when(receiverRepository.existsByUserAndMoneySpray_Token(any(), any()))
                .thenReturn(false);

        throwWhenExhausted(idAsReceiver, token);
    }

    @DisplayName("이력 조회 테스트")
    @ParameterizedTest
    @MethodSource("providedForReceive")
    void historyTest(Identifier idAsSpray,
                     Identifier idAsReceiver,
                     Token token,
                     LocalDateTime current) {
        when(sprayHistoryRepository.findByTokenAndUserAndCreatedAtBetween(
                any(), any(), any(), any()))
                .thenReturn(Optional.empty());

        throwWhenHistoryNotExist(idAsSpray, token, current);
    }

    private Spray makeSpray(Identifier idAsSpray, LocalDateTime localDateTime) {
        return Spray.builder()
                .userId(idAsSpray.getUserId())
                .roomId(idAsSpray.getRoomId())
                .amountUnit(10000)
                .numberOfDistributions(3)
                .current(localDateTime)
                .build();
    }

    private void success(Identifier idAsReceiver, Token token) {
        ReceiveResponseDto expectedReceive = sprayingService.receive(idAsReceiver, token);

        verify(publisher, times(1)).publish(any());
        assertNotNull(expectedReceive);
        assertEquals(expectedReceive.getAmountUnit(), 10000);
    }

    private void throwWhenMine(Identifier idAsSpray, Token token) {
        Exception exception = assertThrows(SprayResponseException.class,
                () -> sprayingService.receive(idAsSpray, token));

        assertTrue(exception.getMessage().contains(SprayErrors.MINE_CANNOT_RECEIVE.getReason()));
    }

    private void throwWhenOverlap(Identifier idAsReceiver, Token token) {
        Exception exception = assertThrows(SprayResponseException.class,
                () -> sprayingService.receive(idAsReceiver, token));

        assertTrue(exception.getMessage().contains(SprayErrors.ONLY_BE_RECEIVED_ONCE.getReason()));
    }

    private void throwWhenExhausted(Identifier idAsReceiver, Token token) {
        Exception exception = assertThrows(SprayResponseException.class,
                () -> sprayingService.receive(idAsReceiver, token));

        assertTrue(exception.getMessage().contains(SprayErrors.ALL_EXHAUSTED.getReason()));
    }

    private void throwWhenExpiration(Identifier idAsReceiver, Token token) {
        Exception exception = assertThrows(SprayResponseException.class,
                () -> sprayingService.receive(idAsReceiver, token));

        assertTrue(exception.getMessage().contains(SprayErrors.EXPIRED_OR_DOES_NOT_EXIST.getReason()));
    }

    private void throwWhenHistoryNotExist(Identifier idAsSpray, Token token, LocalDateTime current) {
        Exception exception = assertThrows(SprayResponseException.class,
                () -> sprayingService.getHistory(token, idAsSpray, current));

        assertTrue(exception.getMessage().contains(SprayErrors.RECEIVED_HISTORY_DOES_NOT_EXIST.getReason()));
    }
}