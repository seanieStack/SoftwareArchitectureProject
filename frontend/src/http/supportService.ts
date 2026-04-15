import { API_PATHS } from '../constants/api'
import { authorizedFetch } from './client'
import type { Borrow, Fine, Notification } from '../types/support'

async function parseErrorResponse(res: Response): Promise<string> {
  const text = await res.text()
  if (!text) return res.statusText || 'Request failed'
  try {
    const body = JSON.parse(text) as { message?: string; error?: string }
    return body.message ?? body.error ?? text
  } catch {
    return text
  }
}

// ── Borrows ───────────────────────────────────────────────────────────────────

export async function createBorrow(userId: number, bookId: number, deadline: string): Promise<Borrow> {
  const res = await authorizedFetch(API_PATHS.BORROWS.CREATE, {
    method: 'POST',
    body: JSON.stringify({ userId, bookId, deadline }),
  })
  if (!res.ok) throw new Error(await parseErrorResponse(res))
  return res.json() as Promise<Borrow>
}

export async function getBorrowsForUser(userId: number): Promise<Borrow[]> {
  const res = await authorizedFetch(API_PATHS.BORROWS.BY_USER(userId))
  if (!res.ok) throw new Error(await parseErrorResponse(res))
  return res.json() as Promise<Borrow[]>
}

export async function returnBorrow(borrowId: number): Promise<Borrow> {
  const res = await authorizedFetch(API_PATHS.BORROWS.RETURN(borrowId), { method: 'PATCH' })
  if (!res.ok) throw new Error(await parseErrorResponse(res))
  return res.json() as Promise<Borrow>
}

// ── Fines ─────────────────────────────────────────────────────────────────────

export async function getFinesForUser(userId: number): Promise<Fine[]> {
  const res = await authorizedFetch(API_PATHS.FINES.BY_USER(userId))
  if (!res.ok) throw new Error(await parseErrorResponse(res))
  return res.json() as Promise<Fine[]>
}

export async function acknowledgeFine(fineId: number): Promise<void> {
  const res = await authorizedFetch(API_PATHS.FINES.ACKNOWLEDGE(fineId), { method: 'PATCH' })
  if (!res.ok) throw new Error(await parseErrorResponse(res))
}

export async function payFine(fineId: number): Promise<void> {
  const res = await authorizedFetch(API_PATHS.FINES.PAY(fineId), { method: 'PATCH' })
  if (!res.ok) throw new Error(await parseErrorResponse(res))
}

// ── Notifications ─────────────────────────────────────────────────────────────

export async function getNotificationsForUser(
  userId: number,
  unreadOnly = false,
): Promise<Notification[]> {
  const path = `${API_PATHS.NOTIFICATIONS.BY_USER(userId)}${unreadOnly ? '?unreadOnly=true' : ''}`
  const res = await authorizedFetch(path)
  if (!res.ok) throw new Error(await parseErrorResponse(res))
  return res.json() as Promise<Notification[]>
}

export async function markNotificationRead(notificationId: number): Promise<void> {
  const res = await authorizedFetch(API_PATHS.NOTIFICATIONS.MARK_READ(notificationId), {
    method: 'PATCH',
  })
  if (!res.ok) throw new Error(await parseErrorResponse(res))
}
