# Security Policy & Implementation: Fire Management System

This document outlines the security architecture, data protection requirements, and coding guidelines for the Fire Management System backend.

---

## 1. Stateless Authentication

The application enforces **stateless REST API security**. 
- Sessions are never created on the server (`SessionCreationPolicy.STATELESS`).
- Authentication context is resolved dynamically for each request via the `JwtAuthenticationFilter`.
- If authentication fails, the response returns a standardized JSON structure with a `401 Unauthorized` status; it never redirects to HTML login pages.

---

## 2. CORS & CSRF Strategy

### CORS (Cross-Origin Resource Sharing)
CORS origins must be strictly controlled to prevent unauthorized websites from executing API requests on behalf of users.
- In **development**, CORS allows origins like `http://localhost:3000` (React dev server).
- In **production**, wildcard origins (`*`) are explicitly prohibited. Origins must be explicitly listed in the environment config `CORS_ALLOWED_ORIGINS`.
- Allowed headers must include standard routing and tracing headers: `Authorization`, `Content-Type`, and `X-Correlation-Id`.

### CSRF (Cross-Site Request Forgery)
Since the API is stateless and does not utilize session-binding cookies (tokens are typically stored in memory/sessionStorage on the React side and passed via the HTTP `Authorization` header), CSRF is disabled (`csrf.disable()`). If session cookies are introduced in the future, a proper anti-CSRF token repository must be implemented.

---

## 3. JWT Specifications & Verification

The token provider validates cryptographic signatures statelessly:

- **Key Verification**: The token signature is validated against the application's signature key.
  - For asymmetric authentication (e.g. Supabase Auth, Keycloak), the public key is retrieved from the JWKS URI.
  - For symmetric signature, a minimum 256-bit signature key must be supplied via `JWT_SECRET`.
- **Claims Extraction**: The token must contain the following claims:
  - `sub`: Unique identifier representing the user principal (e.g., Supabase user UUID).
  - `email`: User email address.
  - `roles`: An array of role strings (e.g., `["ADMIN", "DISPATCHER"]`).
- **Roles Matching**: Roles are parsed and prefixed with `ROLE_` (e.g., `ROLE_ADMIN`) before binding them as `GrantedAuthority` collections in Spring Security.

---

## 4. Secret & Credentials Management

1. **Zero-Secrets Policy**: Raw passwords, JDBC credentials, OIDC client keys, and JWT signatures must never be committed to source control.
2. **Environment Overrides**: All configurations under `resources/` must load environment variables (e.g. `${DATABASE_PASSWORD}`) with reasonable local developer defaults for offline bootstrapping only.
3. **Local .env**: Keep your local secrets in `.env` (which is git-ignored).

---

## 5. Defensive Logging Constraints

To prevent credential leakage through diagnostic tools, the logging configuration enforces these rules:
- **No Sensitive Headers**: Never log HTTP headers containing credentials (e.g. `Authorization`, `Cookie`, `Set-Cookie`).
- **No Password Logging**: Request DTOs containing passwords, MFA pins, or recovery tokens must override `toString()` (or omit Lombok `@ToString`) to prevent field serialization into logs.
- **No Raw Token Logging**: Never print full Bearer tokens or OIDC authorization codes in trace or debug statements. Use truncated trace logging where only the first 10 characters are visible if token tracking is required.
