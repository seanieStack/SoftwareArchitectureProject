/** API path segments (same-origin in dev via Vite proxy, or prefixed with `VITE_BACKEND_URL`). */
export const API_PATHS = {
  // Auth | core-service
  AUTH: {
    LOGIN: '/api/auth/login',
    REGISTER: '/api/auth/register',
    REFRESH: '/api/auth/refresh',
    FORGOT_PASSWORD: '/api/auth/forgot-password',
    RESET_PASSWORD: '/api/auth/reset-password',
  },

  // Books | core-service
  BOOKS: {
    LIST: '/api/books',
    SEARCH: '/api/books/search',
    BY_ID: (bookId: number | string) => `/api/books/${bookId}`,
    AVAILABILITY: (bookId: number | string) => `/api/books/${bookId}/availability`,
  },

  // Borrows | support-service
  BORROWS: {
    BY_USER: (userId: number | string) => `/api/borrows/user/${userId}`,
    BY_ID: (borrowId: number | string) => `/api/borrows/${borrowId}`,
    CREATE: '/api/borrows',
    RETURN: (borrowId: number | string) => `/api/borrows/${borrowId}/return`,
  },

  // Fines | support-service
  FINES: {
    BY_USER: (userId: number | string) => `/api/fines/user/${userId}`,
    BY_ID: (fineId: number | string) => `/api/fines/${fineId}`,
    ACKNOWLEDGE: (fineId: number | string) => `/api/fines/${fineId}/acknowledge`,
    PAY: (fineId: number | string) => `/api/fines/${fineId}/pay`,
  },

  // Notifications | support-service
  NOTIFICATIONS: {
    BY_USER: (userId: number | string) => `/api/notifications/user/${userId}`,
    MARK_READ: (notificationId: number | string) => `/api/notifications/${notificationId}/read`,
  },

  // Admin | core-service + support-service (via api-gateway)
  ADMIN: {
    COUNTS: '/api/admin/counts',
    USERS: '/api/admin/users',
    ANALYTICS: '/api/admin/analytics',
  },
} as const
