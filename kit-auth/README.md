# kit-auth

A lightweight, reusable authentication library for Spring Boot 4 applications. `kit-auth` provides email/password signup and login, BCrypt password hashing, JWT access tokens, optional CSRF protection, and a ready-to-use login/signup page. Your application owns its user database and can supply its own frontend.

Version: `1.0.1` · Java: 21+ · Spring Boot baseline: 4.0.8 · Build: Gradle

> This README documents the implementation included in this repository. The companion consumer application is in [`../kit-auth-demo`](../kit-auth-demo).

## Quick Start

### 1. Add the dependency

In your Spring Boot application's `build.gradle`:

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation 'io.github.si-u-gi:kit-auth:1.0.1'
    }

Use Java 21 and a compatible Spring Boot 4 application. If Maven Central has not finished publishing this version, dependency resolution may temporarily fail.

### 2. Configure a JWT signing key

In `src/main/resources/application.yml`:

    kit:
      auth:
        jwt:
          secret: ${JWT_SECRET}
          access-token-expiration-ms: 3600000

Generate a cryptographically random key (32 bytes or longer for HS256) and Base64-encode it. For example, with PowerShell:

    $bytes = New-Object byte[] 32
    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    $rng.GetBytes($bytes)
    $rng.Dispose()

    $env:JWT_SECRET = [Convert]::ToBase64String($bytes)

    Write-Host "JWT_SECRET = $env:JWT_SECRET"

This sets `JWT_SECRET` for the current PowerShell process and its child processes. If you start the application from IntelliJ IDEA, configure `JWT_SECRET` in its Run Configuration instead. Windows PowerShell 5.1 compatible. Do not commit real signing keys to Git. The same key must be available when validating tokens issued by the application.

### 3. Provide an `AuthUserService` bean

The library deliberately does not provide a database, JPA entity, or repository. Your application must implement:

    package dev.siugi.backendkit.kit_auth.user;

    public interface AuthUserService {
        Optional<AuthUser> findByEmail(String email);
        AuthUser createUser(String email, String passwordHash);
    }

The user representation is `AuthUser(String id, String email, String passwordHash)`.

For a development-only, in-memory implementation, create a Spring-managed class in your application's package:

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

For production: replace this example with a persistent database implementation, enforce a unique normalized-email constraint, and handle duplicate-email races. `passwordHash` is already BCrypt-hashed by `kit-auth`; never hash it again or store the raw password. Return a stable, unique user ID.

### 4. Run the application

    .\gradlew.bat bootRun

By default, open:

- Login: `http://localhost:8080/login`
- Signup: `http://localhost:8080/signup`

Both routes redirect to the bundled UI at `/kit-auth/index.html` (signup uses `?mode=signup`). The pages are enabled unless explicitly disabled.

## API Reference

All request bodies use JSON.

| Method | Endpoint | Purpose | Typical success |
| --- | --- | --- | --- |
| `POST` | `/api/auth/signup` | Register a user | `201 Created` |
| `POST` | `/api/auth/login` | Authenticate and issue an access token | `200 OK` |
| `GET` | `/api/auth/csrf` | Get CSRF token metadata | `200 OK` when enabled |

### Sign up

    POST /api/auth/signup
    Content-Type: application/json

    {"email":"alice@example.com","password":"example-password"}

Success (`201`):

    {"id":"generated-user-id","email":"alice@example.com"}

Validation or duplicate-email errors return `400` with a JSON `message`. The current implementation checks that email/password are nonblank; it does not implement comprehensive email validation or password-strength rules. Add those rules in your application as needed.

### Log in

    POST /api/auth/login
    Content-Type: application/json

    {"email":"alice@example.com","password":"example-password"}

Success (`200`):

    {"accessToken":"eyJ...","tokenType":"Bearer"}

Invalid credentials return `401` with a JSON `message`.

### Authenticate subsequent requests

Send the access token in the `Authorization` header:

    Authorization: Bearer eyJ...

The JWT filter validates the token, uses the token subject as the authenticated principal (user ID), and grants `ROLE_USER`. It does not automatically load additional roles or user details from your database. Tokens have a configurable expiration; the current API does not provide refresh tokens or logout/revocation endpoints.

### CSRF endpoint

    GET /api/auth/csrf

When CSRF protection is enabled, the response includes:

    {
      "headerName": "X-XSRF-TOKEN",
      "parameterName": "_csrf",
      "token": "..."
    }

`headerName` is determined by the configured CSRF token repository; use the returned value, rather than hard-coding it. When CSRF is disabled, the endpoint returns `404` with `{"message":"CSRF protection is disabled"}`.

With CSRF enabled, POST requests require a valid CSRF token even when the endpoint is publicly accessible. Obtain the token before calling signup or login, and send the corresponding cookie/session state along with the request.

## Configuration

Example `application.yml`:

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

| Property | Default | Description |
| --- | --- | --- |
| `kit.auth.jwt.secret` | Not set | Required Base64-encoded HMAC signing key for JWT-based authentication |
| `kit.auth.jwt.access-token-expiration-ms` | `3600000` | Access-token lifetime in milliseconds; must be positive |
| `kit.auth.csrf.enabled` | `true` | Enable Spring Security CSRF protection |
| `kit.auth.csrf.cookie-based` | `true` | Use a readable `XSRF-TOKEN` cookie; when `false`, use `HttpSessionCsrfTokenRepository` |
| `kit.auth.csrf.ignored-paths` | `[]` | Request paths excluded from CSRF checks; use sparingly |
| `kit.auth.security.public-paths` | `[]` | Additional request paths accessible without authentication |
| `kit-auth.page.enabled` | `true` | Enable the bundled `/login` and `/signup` redirect routes |

Important: `kit-auth.page.enabled=false` disables the *redirect routes*, not the bundled static file `/kit-auth/index.html`, and does not automatically make your custom pages public. The built-in security chain permits GET `/login`, `/signup`, and `/kit-auth/`, GET `/api/auth/csrf`, and POST `/api/auth/login` and `/api/auth/signup`. All other requests require authentication unless explicitly listed in `public-paths`.

The default `SecurityFilterChain` is registered only when the application has not supplied its own `SecurityFilterChain` bean. If you define your own chain, you are responsible for allowing the authentication endpoints, configuring CSRF, and registering JWT authentication appropriately.

## Custom Login and Signup Pages

To replace the bundled entry routes with your own UI:

1. Set `kit-auth.page.enabled: false`.
2. Create `src/main/resources/static/login.html` and `signup.html` in your application.
3. Add those paths (and any public CSS/JS assets) to `kit.auth.security.public-paths`.
4. Call the existing `/api/auth/*` endpoints from your frontend.

Here is a complete, minimal `src/main/resources/static/login.html` example using the CSRF endpoint:

    <!DOCTYPE html>
    <html lang="en">
    <head>
      <meta charset="UTF-8">
      <meta name="viewport" content="width=device-width, initial-scale=1">
      <title>Login</title>
    </head>
    <body>
      <h1>Login</h1>
      <form id="loginForm">
        <label for="email">Email</label>
        <input type="email" id="email" autocomplete="username" required>
        <label for="password">Password</label>
        <input type="password" id="password" autocomplete="current-password" required>
        <button type="submit">Log in</button>
      </form>
      <p id="message" role="status" aria-live="polite"></p>

      <script>
        const message = document.getElementById("message");

        async function getCsrf() {
          const response = await fetch("/api/auth/csrf", {
            credentials: "same-origin"
          });
          if (!response.ok) throw new Error("Could not obtain CSRF token");
          const csrf = await response.json();
          if (!csrf.token || !csrf.headerName) {
            throw new Error("Unexpected CSRF response");
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
              message.textContent = "Login failed";
              return;
            }
            const result = await response.json();
            // result.accessToken contains the JWT; result.tokenType is "Bearer".
            // Integrate token handling and navigation with your application's security model.
            message.textContent = "Login successful";
          } catch (error) {
            message.textContent = error.message;
          }
        });
      </script>
    </body>
    </html>

This sample intentionally does not persist the JWT in browser storage. Choose a token-handling strategy appropriate to your application's threat model. A successful login response does not automatically create an authenticated browser session or set a JWT cookie. For subsequent authenticated API calls, your frontend must send the token as a Bearer token.

To implement a custom signup form, submit the same JSON field names (`email`, `password`) to `POST /api/auth/signup`, with the CSRF header and same-origin credentials as shown above. Expect `201 Created` on success.

If you disable CSRF protection, the sample above must be adapted: `GET /api/auth/csrf` will return `404`, and you should omit the CSRF-fetch/header step. Disabling CSRF should be a deliberate security decision, not a workaround for a failing request.

## How It Works

- `AuthUserService` bridges the library to your own user persistence layer.
- `PasswordHashService` uses BCrypt with strength 12.
- `AuthService` handles signup and login, including email normalization and password checks.
- `JwtService` signs and verifies access tokens using a Base64-decoded HMAC key.
- `JwtAuthenticationFilter` authenticates requests carrying a valid `Authorization: Bearer ...` header.
- Auto-configuration registers the API and, when applicable, a default Spring Security filter chain and optional page redirects.

The default security chain uses `SessionCreationPolicy.STATELESS`. This does not remove the need to understand CSRF cookies/sessions when using the configurable CSRF repositories.

## Troubleshooting

| Symptom | What to check |
| --- | --- |
| Gradle cannot find `io.github.si-u-gi:kit-auth:1.0.1` | Confirm `mavenCentral()` is configured, the version is published and indexed, and Gradle is not offline. |
| `401 Unauthorized` on `/` or `/login.html` | Add the desired page path to `kit.auth.security.public-paths`. |
| `401 Unauthorized` on a protected endpoint | Include a valid, unexpired `Authorization: Bearer <token>` header. |
| `403 Forbidden` on signup/login POST | When CSRF is enabled, fetch `/api/auth/csrf` and include its token in the named header; preserve cookie/session state. |
| `/login` no longer redirects | Check `kit-auth.page.enabled`; `false` disables the default route. |
| Authentication endpoints are missing | Ensure `AuthUserService` is a Spring bean and `kit.auth.jwt.secret` is configured. |
| Users disappear after restart | The sample in-memory service is not persistent; implement database-backed storage. |
| Custom security configuration changes behavior | An application-defined `SecurityFilterChain` replaces the library's default chain. |

## Security Notes and Current Limitations

- Use HTTPS in production and keep JWT signing keys out of source control. Rotate keys if exposed.
- Use a sufficiently strong random HMAC signing key and protect token storage against XSS and leakage.
- The example in-memory user store is for demonstration only.
- Implement application-specific email verification, password policy, account recovery, abuse/rate limiting, and persistent user management as needed; these are not provided by the current library.
- JWT refresh, server-side token revocation/logout, and configurable user roles are not included in version `1.0.1`.
- CSRF protection and Bearer-token authentication address different risks. Review your deployment and frontend architecture before changing CSRF settings.
- Public paths should be narrowly scoped. Avoid exposing sensitive endpoints with broad wildcards.

## Example Project and Source

- [Consumer demo](../kit-auth-demo)
- [GitHub repository](https://github.com/si-u-gi/backend-kit)

## License

MIT (as declared in the Maven publication metadata).
