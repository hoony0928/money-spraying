# 카카오페이 사전과제 - 뿌리기 기능 (API) 구현하기

## 개발환경
- Java8
- Spring Boot 2.3.6
- JPA
- Redis
- H2
- Embedded redis
- Junit5
- Gradle

## 요구 사항
* 뿌리기, 받기, 조회 기능을 수행하는 REST API 를 구현
    * 요청한 사용자의 식별값은 숫자 형태이며 "X-USER-ID" 라는 HTTP Header로 전달
    * 요청한 사용자가 속한 대화방의 식별값은 문자 형태이며 "X-ROOM-ID" 라는 HTTP Header로 전달
* 어플리케이션이 다수의 서버에 다수의 인스턴스로 동작하더라도 기능에 문제가 없도록 설계
* 각 기능 및 제약사항에 대한 단위테스트를 반드시 작성

## 상세 구현 요건 및 제약사항
1. 뿌리기 API
    * 뿌릴 금액, 뿌릴 인원을 요청값으로 받는다.
    * 뿌리기 요청 건에 대한 고유 token을 발급하고 응답한다.
    * 뿌릴 금액을 인원수에 맞게 분배하여 저장(분배 로직은 자유롭게 구현)
    * token은 ***3자리 문자열***로 구성되며 ***예측이 불가능***해야 한다.
2. 받기 API
    * 뿌리기 시 발급된 token을 요청값으로 받는다.
    * token에 해당하는 뿌리기 건 중 아직 누구에게도 할당되지 않은 분배건 하나를 API를 호출한 사용자에게 할당하고, 그 금액을 응답한다.
    * 뿌리기 당 ***한 사용자는 한번만*** 받을 수 있다.
    * 자신이 뿌리기한 건은 ***자신이 받을 수 없다***.
    * 뿌리기가 호출된 대화방과 ***동일한 대화방에 속한 사용자만*** 받을 수 있다.
    * 뿌린 건은 ***10분 간만 유효***하다.
3. 조회 API
    * 뿌리기 시 발급된 token으로 요청한다.
    * ***뿌린시각, 뿌린금액, 받기 완료된 금액,받기 완료된 정보([받은금액,받은사용자 아이디] 리스트)를 응답***한다.
    * 뿌린 사람 ***자신만 조회***를 할 수 있다.
    * 뿌린 건에 대한 ***조회는 7일 동안*** 할 수 있다.
    
## 핵심 문제해결 전략 요약
1. 예측 불가능한 3자리 Token 생성하기
    * 각 자리의 문자를 난수를 활용하여 예측 불가능하게 생성한다.
2. 뿌린 건은 10분 간만 유효하다.
    * 짧은 시간동안만 유효하므로, 캐싱 기반으로 구현한다.
    * Token 발급 시 Expiration 정보를 추가하여 만료 체크가 가능하게 한다.
    * Redis Hash 구조를 사용하고, Key의 TTL을 10분으로 설정하여 만료 후 자동 삭제되도록 한다.
3. 뿌릴 금액 분배 및 뿌리기 당 한 사용자만 받기
    * 뿌리기 요청 시 요청 금액 및 받기 금액 단위, 인원 수를 Redis에 저장하고, 인원 수로 분배 처리를 한다.
    * 받기 시 Redis의 Atomic operation 기능을 활용하여 동시성 문제를 해결한다.
    * 받기 시 분배 수를 Atomic operation 으로 차감하고, 0 미만일 경우 모두 소진된 것으로 판단한다.
4. 뿌린 건에 대한 상태 조회
    * 뿌린 건에 대해 최대 7일 동안 데이터를 제공해야 하므로 이력 정보를 RDB에 저장한다.
    * 뿌리기 및 받기 요청 시 저장한다.
5. 각 제약사항에 대해 Mock 기반 Unit Test로 검증한다.

## 테스트 및 API Doc
http://localhost:8000/swagger-ui/

## 구조 설계
### 엔티티 구조
1. 캐싱 모델

╔═══════════════════════════╗
║           Spray           ║
╠═══════════════════════════╣
│  + token                  │
│                           │
│  + roomId                 │
│                           │
│  + userId                 │
│                           │
│  + amount                 │
│                           │
│  + amountUnit             │
│                           │
│  + numberOfDistributions  │
│                           │
│  + createdAt              │
│                           │
│  + expiredAt              │
│                           │
└───────────────────────────┘

2. DB 모델

+---------------------------+                   +---------------------------+
|        MoneySpray         |                   |         Receiver          |
+---------------------------+                   +---------------------------+
|  + token                  |                   |  + receiverId             |
|                           |                   |                           |
|  + user                   |                   |  + token                  |
|                           |                  /|                           |
|  + userId                 |         +------|--|  + user                   |
|                           |         |        \|                           |
|  + money                  |-|-------+         |  + money                  |
|                           |                   |                           |
|  + createdAt              |                   |  + MoneySpray             |
|                           |                   |                           |
|  + expiredAt              |                   +---------------------------+
|                           |                                                
|  + receivers              |                                                
|                           |                                                
+---------------------------+                                                                                                                                           
 
### 전체 구성도기
[전체구성도 바로가기](전체구성도.pdf)                                       
