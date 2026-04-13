import { getBackendBaseUrl } from '../constants/env'
import { logout, refreshAccessToken } from '../redux-features/slices/user/userSlice'
import { store } from '../redux-features/store'

function buildHeaders(token: string | null, init: RequestInit): Headers {
  const headers = new Headers(init.headers)
  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }
  if (init.body !== undefined && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }
  return headers
}

/**
 * Authenticated fetch to the API gateway. Sends `Authorization: Bearer` when a token exists.
 * On 401, attempts a silent token refresh once. If the refresh succeeds the original request
 * is retried with the new access token. If it fails the session is cleared and the user is
 * sent to login.
 */
export async function authorizedFetch(path: string, init: RequestInit = {}): Promise<Response> {
  const base = getBackendBaseUrl().replace(/\/$/, '')
  const url = `${base}${path}`
  const token = store.getState().user.accessToken

  const res = await fetch(url, { ...init, headers: buildHeaders(token, init) })

  if (res.status !== 401 || !store.getState().user.refreshToken) {
    return res
  }

  // Attempt silent refresh
  const result = await store.dispatch(refreshAccessToken())

  if (refreshAccessToken.rejected.match(result)) {
    store.dispatch(logout())
    window.location.assign('/login')
    return res
  }

  // Retry original request with the new access token
  const newToken = store.getState().user.accessToken
  return fetch(url, { ...init, headers: buildHeaders(newToken, init) })
}
