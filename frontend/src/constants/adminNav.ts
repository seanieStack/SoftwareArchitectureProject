import { APP_ROUTES } from './routes'

/** Shared admin sidebar links. */
export const ADMIN_DASHBOARD_NAV = [
  { to: APP_ROUTES.ADMIN_DASHBOARD, label: 'Overview', end: true },
  { to: APP_ROUTES.ADMIN_BOOKS, label: 'Book management' },
  { to: APP_ROUTES.ADMIN_USERS, label: 'User management' },
  { to: APP_ROUTES.ADMIN_ANALYTICS, label: 'Analytics' },
] as const
