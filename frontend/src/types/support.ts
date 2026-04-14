/** Matches support-service BorrowDTO */
export type Borrow = {
  id: number
  userId: number
  bookId: number
  status: 'BORROWED' | 'OVERDUE' | 'RETURNED'
  borrowedAt: string
  deadline: string
  returnedAt: string | null
}

/** Matches support-service FineDTO */
export type Fine = {
  id: number
  borrowId: number
  userId: number
  amount: number
  issuedAt: string
  paid: boolean
  paidAt: string | null
  acknowledged: boolean
}

/** Matches support-service NotificationDTO */
export type Notification = {
  id: number
  userId: number
  type: string
  message: string
  read: boolean
  createdAt: string
}
