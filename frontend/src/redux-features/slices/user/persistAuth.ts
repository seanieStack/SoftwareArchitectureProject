import { AUTH_STORAGE_KEYS } from '../../../constants/authStorage'
import { userProfileSchema, type AuthResponse, type UserProfile } from './authTypes'

function clearSessionStorageAuth(): void {
  sessionStorage.removeItem(AUTH_STORAGE_KEYS.ACCESS_TOKEN)
  sessionStorage.removeItem(AUTH_STORAGE_KEYS.USER_PROFILE)
  sessionStorage.removeItem(AUTH_STORAGE_KEYS.REFRESH_TOKEN)
}

function clearLocalStorageAuth(): void {
  localStorage.removeItem(AUTH_STORAGE_KEYS.ACCESS_TOKEN)
  localStorage.removeItem(AUTH_STORAGE_KEYS.USER_PROFILE)
  localStorage.removeItem(AUTH_STORAGE_KEYS.REFRESH_TOKEN)
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
  if (response.refreshToken) {
    storage.setItem(AUTH_STORAGE_KEYS.REFRESH_TOKEN, response.refreshToken)
  }
}

export function persistAccessToken(accessToken: string, refreshToken: string): void {
  // Update tokens in whichever storage is active without disturbing the profile or rememberMe choice
  if (sessionStorage.getItem(AUTH_STORAGE_KEYS.ACCESS_TOKEN) !== null) {
    sessionStorage.setItem(AUTH_STORAGE_KEYS.ACCESS_TOKEN, accessToken)
    sessionStorage.setItem(AUTH_STORAGE_KEYS.REFRESH_TOKEN, refreshToken)
  } else {
    localStorage.setItem(AUTH_STORAGE_KEYS.ACCESS_TOKEN, accessToken)
    localStorage.setItem(AUTH_STORAGE_KEYS.REFRESH_TOKEN, refreshToken)
  }
}

export function loadPersistedRefreshToken(): string | null {
  return (
    sessionStorage.getItem(AUTH_STORAGE_KEYS.REFRESH_TOKEN) ??
    localStorage.getItem(AUTH_STORAGE_KEYS.REFRESH_TOKEN)
  )
}

export function clearPersistedAuth(): void {
  clearSessionStorageAuth()
  clearLocalStorageAuth()
}

export function loadPersistedAuth(): { accessToken: string | null; profile: UserProfile | null; refreshToken: string | null } {
  const sessionToken = sessionStorage.getItem(AUTH_STORAGE_KEYS.ACCESS_TOKEN)
  const localToken = localStorage.getItem(AUTH_STORAGE_KEYS.ACCESS_TOKEN)

  if (sessionToken) {
    const raw = sessionStorage.getItem(AUTH_STORAGE_KEYS.USER_PROFILE)
    const profile = parseProfile(raw)
    if (!profile) {
      clearSessionStorageAuth()
      return { accessToken: null, profile: null, refreshToken: null }
    }
    const refreshToken = sessionStorage.getItem(AUTH_STORAGE_KEYS.REFRESH_TOKEN)
    return { accessToken: sessionToken, profile, refreshToken }
  }

  if (localToken) {
    const raw = localStorage.getItem(AUTH_STORAGE_KEYS.USER_PROFILE)
    const profile = parseProfile(raw)
    if (!profile) {
      clearLocalStorageAuth()
      return { accessToken: null, profile: null, refreshToken: null }
    }
    const refreshToken = localStorage.getItem(AUTH_STORAGE_KEYS.REFRESH_TOKEN)
    return { accessToken: localToken, profile, refreshToken }
  }

  return { accessToken: null, profile: null, refreshToken: null }
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
