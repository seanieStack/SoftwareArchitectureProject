import { API_PATHS } from '../constants/api'
import { authorizedFetch } from './client'
import type { AdminUser, AdminCounts, AdminAnalytics } from '../types/admin'

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

export async function getAdminCounts(): Promise<AdminCounts> {
  const res = await authorizedFetch(API_PATHS.ADMIN.COUNTS)
  if (!res.ok) throw new Error(await parseErrorResponse(res))
  return res.json() as Promise<AdminCounts>
}

export async function getAdminUsers(): Promise<AdminUser[]> {
  const res = await authorizedFetch(API_PATHS.ADMIN.USERS)
  if (!res.ok) throw new Error(await parseErrorResponse(res))
  return res.json() as Promise<AdminUser[]>
}

export async function getAdminAnalytics(): Promise<AdminAnalytics> {
  const res = await authorizedFetch(API_PATHS.ADMIN.ANALYTICS)
  if (!res.ok) throw new Error(await parseErrorResponse(res))
  return res.json() as Promise<AdminAnalytics>
}
