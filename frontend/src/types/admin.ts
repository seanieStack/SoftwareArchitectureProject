export interface AdminUser {
  id: number
  email: string
  fullName: string
  userType: string
}

export interface AdminCounts {
  totalUsers: number
  totalStudents: number
  totalStaff: number
  totalAdmins: number
  totalBooks: number
}

export interface AdminAnalytics {
  activeLoans: number
  returnedLoans: number
  overdueLoans: number
  outstandingFines: number
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
