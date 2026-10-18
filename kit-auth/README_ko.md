# kit-auth

Spring Boot 4 애플리케이션에서 재사용할 수 있는 경량 인증 라이브러리입니다. `kit-auth`는 이메일/비밀번호 기반 회원가입과 로그인, BCrypt 비밀번호 해싱, JWT 액세스 토큰, 선택적으로 사용할 수 있는 CSRF 보호, 기본 로그인·회원가입 페이지를 제공합니다. 사용자 데이터베이스는 애플리케이션에서 직접 관리하며, 프런트엔드도 자유롭게 구현할 수 있습니다.

버전: `1.0.1` · Java: 21 이상 · Spring Boot 기준 버전: 4.0.8 · 빌드: Gradle

> 이 문서는 저장소에 포함된 구현을 기준으로 작성되었습니다. 라이브러리를 사용하는 예제 프로젝트는 [`../demo`](../demo)를 참고하세요.

## 빠른 시작 (Quick Start)

### 1. 의존성 추가

Spring Boot 애플리케이션의 `build.gradle`에 추가합니다.

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation 'io.github.si-u-gi:kit-auth:1.0.1'
    }

Java 21 및 호환되는 Spring Boot 4 환경을 사용하세요. Maven Central에서 해당 버전의 게시가 완료되지 않았다면 일시적으로 의존성 검색에 실패할 수 있습니다.

### 2. JWT 서명 키 설정

`src/main/resources/application.yml`:

    kit:
      auth:
        jwt:
          secret: ${JWT_SECRET}
          access-token-expiration-ms: 3600000

HS256에 사용할 32바이트 이상의 암호학적으로 안전한 무작위 키를 생성하고 Base64로 인코딩하세요. Windows PowerShell 5.1에서도 실행할 수 있는 명령어는 다음과 같습니다.

    $bytes = New-Object byte[] 32
    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    $rng.GetBytes($bytes)
    $rng.Dispose()

    $env:JWT_SECRET = [Convert]::ToBase64String($bytes)

    Write-Host "JWT_SECRET = $env:JWT_SECRET"

이 환경변수는 현재 PowerShell 프로세스 및 해당 프로세스에서 실행한 자식 프로세스에 적용됩니다. IntelliJ IDEA에서 애플리케이션을 실행한다면 Run Configuration에도 `JWT_SECRET`을 등록해야 합니다. 실제 서명 키를 Git에 커밋하지 마세요. 기존 토큰을 검증하려면 발급 당시 사용한 서명 키가 필요합니다.

### 3. `AuthUserService` 빈 구현

라이브러리는 데이터베이스, JPA 엔티티, 리포지토리를 직접 제공하지 않습니다. 애플리케이션에서 다음 인터페이스를 구현해야 합니다.

    package dev.siugi.backendkit.kit_auth.user;

    public interface AuthUserService {
        Optional<AuthUser> findByEmail(String email);
        AuthUser createUser(String email, String passwordHash);
    }

사용자 데이터 구조는 `AuthUser(String id, String email, String passwordHash)`입니다.

다음은 개발 및 테스트 전용 인메모리 구현 예제입니다. 애플리케이션의 컴포넌트 스캔 대상 패키지에 클래스를 작성하세요.

    import dev.siugi.backendkit.kit_auth.user.AuthUser;
    import dev.siugi.backendkit.kit_auth.user.AuthUserService;
    import org.springframework.stereotype.Service;

    import java.util.Locale;
    import java.util.Optional;
    import java.util.UUID;
    import java.util.concurrent.ConcurrentHashMap;
    import java.util.concurrent.ConcurrentMap;

    @Service
    public class DemoAuthUserService implements AuthUserService {
        private final ConcurrentMap<String, AuthUser> users = new ConcurrentHashMap<>();

        @Override
        public Optional<AuthUser> findByEmail(String email) {
            if (email == null) return Optional.empty();
            return Optional.ofNullable(users.get(email.trim().toLowerCase(Locale.ROOT)));
        }

        @Override
        public AuthUser createUser(String email, String passwordHash) {
            String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
            AuthUser user = new AuthUser(UUID.randomUUID().toString(), normalizedEmail, passwordHash);
            if (users.putIfAbsent(normalizedEmail, user) != null) {
                throw new IllegalArgumentException("Email already registered");
            }
            return user;
        }
    }

운영 환경에서는 데이터베이스에 영구 저장하도록 구현하고, 정규화된 이메일에 고유 제약조건을 적용하며, 동시 회원가입으로 인한 중복도 처리해야 합니다. `passwordHash`는 `kit-auth`에서 이미 BCrypt로 해싱한 값이므로 다시 해싱하거나 원본 비밀번호를 저장하지 마세요. 사용자 ID는 고유하고 안정적으로 유지해야 합니다.

### 4. 애플리케이션 실행

    .\gradlew.bat bootRun

기본 포트가 8080이라면 다음 주소로 접속할 수 있습니다.

- 로그인: `http://localhost:8080/login`
- 회원가입: `http://localhost:8080/signup`

두 경로는 내장 UI인 `/kit-auth/index.html`로 리다이렉트됩니다. 회원가입 페이지에는 `?mode=signup` 쿼리가 추가됩니다. 기본 페이지는 명시적으로 비활성화하지 않는 한 활성화됩니다.

## API 명세

요청 본문은 JSON 형식입니다.

| 메서드 | 엔드포인트 | 기능 | 일반적인 성공 응답 |
| --- | --- | --- | --- |
| `POST` | `/api/auth/signup` | 회원가입 | `201 Created` |
| `POST` | `/api/auth/login` | 로그인 및 액세스 토큰 발급 | `200 OK` |
| `GET` | `/api/auth/csrf` | CSRF 토큰 정보 조회 | 활성화 시 `200 OK` |

### 회원가입

    POST /api/auth/signup
    Content-Type: application/json

    {"email":"alice@example.com","password":"example-password"}

성공 시 (`201`):

    {"id":"generated-user-id","email":"alice@example.com"}

입력 검증 실패 또는 중복 이메일 오류는 `message`가 포함된 JSON과 함께 `400`을 반환합니다. 현재 구현에서는 이메일과 비밀번호가 비어 있지 않은지 검사하지만, 완전한 이메일 형식 검증이나 비밀번호 강도 정책은 제공하지 않습니다. 필요한 경우 애플리케이션에서 추가하세요.

### 로그인

    POST /api/auth/login
    Content-Type: application/json

    {"email":"alice@example.com","password":"example-password"}

성공 시 (`200`):

    {"accessToken":"eyJ...","tokenType":"Bearer"}

인증 정보가 올바르지 않으면 `message`가 포함된 JSON과 함께 `401`을 반환합니다.

### 로그인 이후 인증 요청

보호된 API에 요청할 때는 `Authorization` 헤더에 액세스 토큰을 전달합니다.

    Authorization: Bearer eyJ...

JWT 필터는 토큰을 검증하고, 토큰의 subject를 인증 주체(사용자 ID)로 사용하며 `ROLE_USER` 권한을 부여합니다. 데이터베이스에서 추가 역할이나 사용자 정보를 자동 조회하지는 않습니다. 토큰 만료 시간은 설정할 수 있지만, 현재 API에는 리프레시 토큰 및 로그아웃/토큰 폐기 엔드포인트가 없습니다.

### CSRF 토큰 API

    GET /api/auth/csrf

CSRF 보호가 활성화되어 있다면 다음 형식의 응답을 반환합니다.

    {
      "headerName": "X-XSRF-TOKEN",
      "parameterName": "_csrf",
      "token": "..."
    }

`headerName`은 CSRF 토큰 저장소 설정에 따라 달라질 수 있으므로 응답에서 받은 값을 사용하세요. CSRF가 비활성화되어 있으면 `{"message":"CSRF protection is disabled"}`와 함께 `404`를 반환합니다.

CSRF가 활성화된 경우 공개 API라 하더라도 POST 요청에는 유효한 CSRF 토큰이 필요합니다. 로그인이나 회원가입 요청 전에 토큰을 발급받고 관련 쿠키/세션 상태를 함께 전달해야 합니다.

## 설정 (Configuration)

`application.yml` 예제:

    kit:
      auth:
        jwt:
          secret: ${JWT_SECRET}
          access-token-expiration-ms: 3600000
        csrf:
          enabled: true
          cookie-based: true
          ignored-paths: []
        security:
          public-paths:
            - /
            - /index.html
            - /login.html
            - /signup.html
            - /css/
            - /js/
            - /images/

    kit-auth:
      page:
        enabled: false

| 설정 | 기본값 | 설명 |
| --- | --- | --- |
| `kit.auth.jwt.secret` | 미설정 | JWT 서명에 필요한 Base64 인코딩 HMAC 키 |
| `kit.auth.jwt.access-token-expiration-ms` | `3600000` | 액세스 토큰 유효기간(밀리초), 양수여야 함 |
| `kit.auth.csrf.enabled` | `true` | Spring Security CSRF 보호 활성화 |
| `kit.auth.csrf.cookie-based` | `true` | 읽을 수 있는 `XSRF-TOKEN` 쿠키 사용. `false`이면 `HttpSessionCsrfTokenRepository` 사용 |
| `kit.auth.csrf.ignored-paths` | `[]` | CSRF 검사에서 제외할 요청 경로. 신중하게 설정할 것 |
| `kit.auth.security.public-paths` | `[]` | 로그인하지 않은 사용자도 접근할 수 있는 추가 경로 |
| `kit-auth.page.enabled` | `true` | 기본 `/login`, `/signup` 리다이렉트 라우트 활성화 |

중요: `kit-auth.page.enabled=false`는 기본 리다이렉트 라우트만 비활성화합니다. 내장 정적 파일 `/kit-auth/index.html`을 제거하지 않으며, 사용자 정의 페이지를 자동으로 공개하지도 않습니다.

기본 보안 필터 체인은 `GET /login`, `GET /signup`, `GET /kit-auth/`, `GET /api/auth/csrf`, `POST /api/auth/login`, `POST /api/auth/signup`을 인증 없이 허용합니다. 그 외 경로는 `public-paths`에 추가하지 않으면 인증이 필요합니다.

라이브러리의 기본 `SecurityFilterChain`은 애플리케이션에서 별도의 `SecurityFilterChain` 빈을 정의하지 않은 경우에만 등록됩니다. 직접 보안 체인을 구현한다면 인증 API 허용, CSRF 설정, JWT 인증 필터 구성도 직접 관리해야 합니다.

## 사용자 정의 로그인·회원가입 페이지

내장 로그인 진입 경로 대신 프로젝트 자체 UI를 사용하려면 다음과 같이 설정하세요.

1. `kit-auth.page.enabled: false`를 설정합니다.
2. 애플리케이션의 `src/main/resources/static/` 아래에 `login.html`, `signup.html`을 만듭니다.
3. 해당 페이지 및 공개 CSS/JS 경로를 `kit.auth.security.public-paths`에 추가합니다.
4. 프런트엔드에서 기존 `/api/auth/*` API를 호출합니다.

아래는 CSRF API를 사용하는 `src/main/resources/static/login.html` 전체 예제입니다.

    <!DOCTYPE html>
    <html lang="ko">
    <head>
      <meta charset="UTF-8">
      <meta name="viewport" content="width=device-width, initial-scale=1">
      <title>로그인</title>
    </head>
    <body>
      <h1>로그인</h1>
      <form id="loginForm">
        <label for="email">이메일</label>
        <input type="email" id="email" autocomplete="username" required>
        <label for="password">비밀번호</label>
        <input type="password" id="password" autocomplete="current-password" required>
        <button type="submit">로그인</button>
      </form>
      <p id="message" role="status" aria-live="polite"></p>

      <script>
        const message = document.getElementById("message");

        async function getCsrf() {
          const response = await fetch("/api/auth/csrf", {
            credentials: "same-origin"
          });
          if (!response.ok) throw new Error("CSRF 토큰을 가져오지 못했습니다.");
          const csrf = await response.json();
          if (!csrf.token || !csrf.headerName) {
            throw new Error("CSRF 응답 형식이 올바르지 않습니다.");
          }
          return csrf;
        }

        document.getElementById("loginForm").addEventListener("submit", async (event) => {
          event.preventDefault();
          message.textContent = "";
          try {
            const csrf = await getCsrf();
            const response = await fetch("/api/auth/login", {
              method: "POST",
              credentials: "same-origin",
              headers: {
                "Content-Type": "application/json",
                [csrf.headerName]: csrf.token
              },
              body: JSON.stringify({
                email: document.getElementById("email").value,
                password: document.getElementById("password").value
              })
            });
            if (!response.ok) {
              message.textContent = "로그인에 실패했습니다.";
              return;
            }
            const result = await response.json();
            // result.accessToken에 JWT가, result.tokenType에 "Bearer"가 담깁니다.
            // 토큰 처리 및 페이지 이동은 애플리케이션의 보안 정책에 맞게 구현하세요.
            message.textContent = "로그인 성공";
          } catch (error) {
            message.textContent = error.message;
          }
        });
      </script>
    </body>
    </html>

이 예제는 의도적으로 JWT를 브라우저 저장소에 보관하지 않습니다. 애플리케이션의 위협 모델에 맞게 토큰 저장·전달 방식을 선택하세요. 로그인 성공만으로 브라우저 세션이 자동 생성되거나 JWT 쿠키가 설정되는 것은 아닙니다. 이후 보호된 API를 호출할 때는 프런트엔드가 Bearer 토큰을 전달해야 합니다.

사용자 정의 회원가입 페이지에서는 위 예제와 동일하게 CSRF 헤더 및 same-origin credentials를 사용하면서 `POST /api/auth/signup`으로 `email`, `password` JSON을 보내면 됩니다. 성공 시 `201 Created`를 반환합니다.

CSRF 보호를 비활성화했다면 예제를 변경해야 합니다. 이 경우 `GET /api/auth/csrf`가 `404`를 반환하므로 CSRF 조회 및 헤더 추가 단계를 생략해야 합니다. CSRF 비활성화는 요청 오류를 회피하기 위한 임시 조치가 아니라 보안 설계를 검토한 뒤 결정해야 합니다.

## 내부 동작

- `AuthUserService`: 라이브러리와 애플리케이션의 사용자 저장소를 연결합니다.
- `PasswordHashService`: BCrypt 강도 12로 비밀번호를 해싱합니다.
- `AuthService`: 이메일 정규화, 비밀번호 확인을 포함한 회원가입 및 로그인 처리를 담당합니다.
- `JwtService`: Base64 디코딩된 HMAC 키로 액세스 토큰을 서명하고 검증합니다.
- `JwtAuthenticationFilter`: `Authorization: Bearer ...` 헤더에 유효한 JWT가 포함된 요청을 인증합니다.
- 자동 설정: 인증 API와 필요한 경우 기본 Spring Security 필터 체인 및 선택적인 페이지 리다이렉트를 등록합니다.

기본 보안 체인은 `SessionCreationPolicy.STATELESS`를 사용합니다. 다만 설정 가능한 CSRF 저장소에서 쿠키 또는 세션을 사용하는 경우, 해당 동작도 이해하고 구성해야 합니다.

## 문제 해결 (Troubleshooting)

| 증상 | 확인할 사항 |
| --- | --- |
| Gradle에서 `io.github.si-u-gi:kit-auth:1.0.1`을 찾지 못함 | `mavenCentral()` 설정, 게시 상태, Gradle 오프라인 모드 확인 |
| `/` 또는 `/login.html`에서 `401 Unauthorized` | 해당 경로를 `kit.auth.security.public-paths`에 추가 |
| 보호된 API에서 `401 Unauthorized` | 만료되지 않은 올바른 `Authorization: Bearer <token>` 헤더 전달 여부 확인 |
| 로그인/회원가입 POST에서 `403 Forbidden` | CSRF 활성화 시 `/api/auth/csrf` 토큰을 응답의 헤더 이름으로 전달하고 쿠키/세션 유지 |
| `/login`이 더 이상 리다이렉트되지 않음 | `kit-auth.page.enabled=false`인지 확인 |
| 인증 API가 등록되지 않음 | `AuthUserService`가 Spring 빈인지, `kit.auth.jwt.secret`이 설정됐는지 확인 |
| 재시작 후 사용자가 사라짐 | 인메모리 예제 대신 DB 기반 저장소 구현 |
| 자체 보안 설정 후 동작이 달라짐 | 애플리케이션이 정의한 `SecurityFilterChain`이 라이브러리 기본 체인을 대체하는지 확인 |

## 보안 유의사항 및 현재 제한 사항

- 운영 환경에서는 HTTPS를 사용하고 JWT 서명 키를 소스 코드 저장소에 포함하지 마세요. 노출된 키는 교체하세요.
- 충분히 강한 무작위 HMAC 키를 사용하고 XSS 및 토큰 유출 위험을 고려해 토큰을 관리하세요.
- 인메모리 사용자 저장소 예제는 시연용이며 운영 환경에 적합하지 않습니다.
- 이메일 인증, 비밀번호 정책, 계정 복구, 요청 횟수 제한, 영구 사용자 관리는 필요에 따라 애플리케이션에서 구현해야 합니다.
- 버전 `1.0.1`에는 JWT 리프레시, 서버 측 토큰 폐기/로그아웃, 사용자별 권한 구성 기능이 포함되지 않습니다.
- CSRF 보호와 Bearer 토큰 인증은 서로 다른 위험을 다룹니다. 배포 구조와 프런트엔드 인증 방식을 검토한 후 CSRF 설정을 변경하세요.
- 공개 경로는 필요한 범위로 제한하세요. 과도하게 넓은 와일드카드로 민감한 API를 노출하지 마세요.

## 예제 프로젝트 및 소스 코드

- [사용 예제 프로젝트](../demo)
- [GitHub 저장소](https://github.com/si-u-gi/backend-kit)

## 라이선스

MIT (Maven 게시 메타데이터 기준).
