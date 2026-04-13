import { AUTH_STORAGE_KEYS } from '../../../constants/authStorage'
import { userProfileSchema, type AuthResponse, type UserProfile } from './authTypes'

function clearSessionStorageAuth(): void {
  sessionStorage.removeItem(AUTH_STORAGE_KEYS.ACCESS_TOKEN)
  sessionStorage.removeItem(AUTH_STORAGE_KEYS.USER_PROFILE)
}

function clearLocalStorageAuth(): void {
  localStorage.removeItem(AUTH_STORAGE_KEYS.ACCESS_TOKEN)
  localStorage.removeItem(AUTH_STORAGE_KEYS.USER_PROFILE)
}

/** Remove token from the storage we are not using for this session. */
function clearOppositeStorage(rememberMe: boolean): void {
  if (rememberMe) {
    clearSessionStorageAuth()
  } else {
    clearLocalStorageAuth()
  }
}

export function persistAuthSession(response: AuthResponse, rememberMe: boolean): void {
  clearOppositeStorage(rememberMe)
  const storage = rememberMe ? localStorage : sessionStorage
  storage.setItem(AUTH_STORAGE_KEYS.ACCESS_TOKEN, response.accessToken)
  storage.setItem(AUTH_STORAGE_KEYS.USER_PROFILE, JSON.stringify(response.user))
}

export function clearPersistedAuth(): void {
  clearSessionStorageAuth()
  clearLocalStorageAuth()
}

export function loadPersistedAuth(): { accessToken: string | null; profile: UserProfile | null } {
  const sessionToken = sessionStorage.getItem(AUTH_STORAGE_KEYS.ACCESS_TOKEN)
  const localToken = localStorage.getItem(AUTH_STORAGE_KEYS.ACCESS_TOKEN)

  if (sessionToken) {
    const raw = sessionStorage.getItem(AUTH_STORAGE_KEYS.USER_PROFILE)
    const profile = parseProfile(raw)
    if (!profile) {
      clearSessionStorageAuth()
      return { accessToken: null, profile: null }
    }
    return { accessToken: sessionToken, profile }
  }

  if (localToken) {
    const raw = localStorage.getItem(AUTH_STORAGE_KEYS.USER_PROFILE)
    const profile = parseProfile(raw)
    if (!profile) {
      clearLocalStorageAuth()
      return { accessToken: null, profile: null }
    }
    return { accessToken: localToken, profile }
  }

  return { accessToken: null, profile: null }
}

function parseProfile(raw: string | null): UserProfile | null {
  if (!raw) return null
  try {
    const parsed = JSON.parse(raw) as unknown
    const result = userProfileSchema.safeParse(parsed)
    return result.success ? result.data : null
  } catch {
    return null
  }
}
