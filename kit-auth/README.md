# kit-auth

Authentication endpoints: `POST /api/auth/signup`, `POST /api/auth/login`, `GET /api/auth/csrf`.
Default login page: `/login`; signup page: `/signup`.

Currently requires an `AuthUserService` bean and `kit.auth.jwt.secret` (Base64 signing key).
For a working consumer example, see `../demo`.
