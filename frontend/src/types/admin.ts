/** GET /api/admin/counts (core-service) */
export type AdminCounts = {
  totalBooks: number
  registeredUsers: number
}

/** GET /api/admin/users (core-service) */
export type AdminUser = {
  id: number
  email: string
  fullName: string
  role: 'STUDENT' | 'STAFF' | 'ADMIN'
  createdAt: string
}

/** GET /api/admin/analytics (support-service) */
export type AdminAnalytics = {
  totalBorrows: number
  activeBorrows: number
  overdueBorrows: number
  totalFinesCollected: number
  unpaidFines: number
}
