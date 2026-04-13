/** API path segments (same-origin in dev via Vite proxy, or prefixed with `VITE_BACKEND_URL`). */
export const API_PATHS = {
  LOGIN: '/api/auth/login',
  REGISTER: '/api/auth/register',
  REFRESH: '/api/auth/refresh',
  FORGOT_PASSWORD: '/api/auth/forgot-password',
  RESET_PASSWORD: '/api/auth/reset-password',
} as const
