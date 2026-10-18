# backend-kit

Spring Boot 4 authentication library and consumer demo.

## Structure
- `kit-auth/`: reusable auth library with login/signup UI, JWT and CSRF.
- `kit-auth-demo`: integration demo. Its `DemoAuthUserService` stores users **only in memory**.

## Run locally (PowerShell)
Set a unique Base64-encoded 32-byte-or-longer JWT signing key (do not commit it):

```powershell
$bytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
cd kit-auth
.\gradlew.bat publishToMavenLocal
cd ..\demo
.\gradlew.bat --refresh-dependencies bootRun
```

Visit `http://localhost:8080/login` or `/signup`.

## Notes
- The demo is not a production user database: users disappear after restart.
- `kit-auth` requires a consumer-provided `AuthUserService` implementation today.
- This cleanup does not yet add a JPA default user store or MongoDB support.
- JWT is returned in the response body, not persisted across browser refreshes.
- Applications providing their own `SecurityFilterChain` must explicitly integrate the JWT filter and auth route rules.
- Never put real JWT secrets in source control.



그다음 kit-auth에서:
.\gradlew.bat publishToMavenLocal

demo에서:
.\gradlew.bat --refresh-dependencies clean bootRun

http://localhost:8080/kit-auth/index.html