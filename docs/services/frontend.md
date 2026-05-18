# frontend

React 19 + Vite + TS + Redux Toolkit + Tailwind + Zod + React Hook Form. Dev port 5173.

## Layout

```
src/
  main.tsx              entry
  http/                 fetch helpers (client.ts has auto-refresh)
  router/               react router setup + auth guards
  redux-features/       store + user slice
  views/                pages + components
  layout/               shells
  constants/            api paths, routes, env
  validation-schemas/   zod schemas for forms
  types/                types for catalog, support, admin
```

## Routes

| Path | Page | Guard |
|---|---|---|
| `/login` | Login | guest |
| `/register` | Register | guest |
| `/forgot-password` | Forgot | - |
| `/reset-password` | Reset | - |
| `/dashboard/student` | Student dashboard | auth |
| `/browse` | Browse books | auth |
| `/admin` | Admin home | admin |
| `/admin/books` | Books mgmt | admin |
| `/admin/users` | Users mgmt | admin |
| `/admin/analytics` | Analytics | admin |

Guards: `RequireAuth`, `RequireAdmin`, `GuestRoute`, `RootRedirect`. All in `router/AuthRoutes.tsx`.

## State

One Redux slice (`user`) with the access token, refresh token and profile. Thunks for login/register/refresh.

`persistAuth.ts` saves to localStorage if rememberMe is on, else sessionStorage.

## HTTP

`http/client.ts` has `authorizedFetch`:
1. attaches `Authorization: Bearer ...`
2. on 401 + refresh token present, trys to refresh once
3. if refresh fails, logout and redirect to /login

The auth endpoints themselves use plain fetch (no token needed yet).

## Vite proxy

`vite.config.ts` proxies `/api/*` to the gateway so the browser sees same-origin requests:

```ts
proxy: {
  '/api': { target: env.VITE_PROXY_TARGET || 'http://localhost:8080', changeOrigin: true }
}
```

## Scripts

```bash
npm run dev      # dev server
npm run build    # tsc + vite build
npm run lint     # eslint
```
