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
}
