# Netty Radius Server ([TinyRadius](https://github.com/ctran/TinyRadius) + [Netty](https://netty.io) + [Keycloak](https://keycloak.org))
  - Netty Radius Server는 [Netty](https://netty.io)의 Handler를 [TinyRadius](https://github.com/ctran/TinyRadius)를 이용해 구현한 Radius Server 입니다.
  - Kotlin으로 작성되었습니다. 
  - [Keycloak](https://keycloak.org) 사용자 인증을 기본으로 제공 합니다.

## 1. 실행
  - 방법1. `net.ruinnel.radius.server.Application.kt` 를 실행합니다.
  - 방법2. `./gradlew jar`로 jar로 빌드 후 jar 파일을 실행합니다.
  - 설정 환경 변수 - 기본값은 `net.ruinnel.radius.server.Application.kt`의 `default*` 필드들 참조.
    - Netty 설정
      - NETTY_THREAD_COUNT(0): Netty 쓰레드 숫자
    - Radius Server 설정
      - RADIUS_AUTH_PORT: Radius 인증 서버 포트 번호
      - RADIUS_ACCT_PORT: Radius 계정 서버 포트 번호
      - RADIUS_SHARED_SECRET: Radius 서버 공유키
    - Keycloak 관련 설정
      - USE_OTP: OTP 사용 여부
      - SECRET_KEY: OTP 사용시 state 암/복호화에 사용할 키
      - REPLY_MESSAGE: OTP 요청시 Prompt 문구
      - KEYCLOAK_URL: Keycloak 인증 URL
      - KEYCLOAK_REALM: Keycloak Realm
      - KEYCLOAK_CLIENT_ID: Keycloak Client ID
      - KEYCLOAK_CLIENT_SECRET: Keycloak Client Secret
  
## 2. Custom 인증 방식 구현
 - `net.ruinnel.radius.server.Authenticator` 인터페이스를 구현하면 됩니다.
 - 아래 4개의 메소드로 구성되어 있습니다.

```kotlin
fun authenticate(username: String, password: String, otp: String?): Boolean
fun useOtp(nasIdentifier: String?): Boolean
fun secretKey(): String
fun replyMessage(): String
```

  - `authenticate`: username, password가 전달 됩니다. otp는 `useOtp`가 true일 경우가 아니면 null 입니다.
  - `useOtp`: Otp 사용 여부.
  - `secretKey`: `useOtp`가 true 일 경우 state 암/복호화에 사용됩니다.
    - Netty Radius Server는 Stateless 하게 통신을 하며, `useOtp`가 true일 경우 통신이 2번 발생합니다.
    - Radius Client는 `state` Attribute로 받은 값을 다음번 요청시 그대로 보내게 구현되어 있습니다.
      - 1회차 요청시(username/password)를 받은 후 password를 `secretKey`로 암호화 후 `state` Attribute에 담아 `Access Challenge` Response를 보냅니다.
      - Radius Client(ex: [pam_radius_auth](https://github.com/FreeRADIUS/pam_radius))는 사용자에게 prompt를 띄워 otp를 입력받아 다시 Radius Server로 요청을 보냅니다. (이때 `state` Attribute는 서버에서 받은 값을 그대로 전송 합니다.)
      - 2회차 요청시(username/otp)를 받은 후 `state` Attribute를 `secretKey`로 복호화합니다.
      - username / password / otp를 `authenticator`에 전달 합니다.
  - `replyMessage`: Otp 요청시 prompt 메세지를 설정 합니다.

## 3. Custom 인증 방식 적용
  - `net.ruinnel.radius.server.NettyRadiusServer` 사용시
    - `net.ruinnel.radius.server.Application.kt` 소스 참조.
    - `authenticator`를 직접 구현한 객체로 변경하면 됩니다.
  - 사용 중인 netty 서버에 Handler 추가
    - `net.ruinnel.radius.server.NettyRadiusServer` 코드 참조
    - `net.ruinnel.radius.server.RadiusServerHandler`생성시 구현한 `authenticator` 객체 전달.