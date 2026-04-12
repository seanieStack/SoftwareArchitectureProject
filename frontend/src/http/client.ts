import { getBackendBaseUrl } from '../constants/env'
import { logout } from '../redux-features/slices/user/userSlice'
import { store } from '../redux-features/store'

/**
 * Authenticated fetch to the API gateway. Sends `Authorization: Bearer` when a token exists.
 * On 401, clears auth and sends the browser to the login page.
 */
export async function authorizedFetch(path: string, init: RequestInit = {}): Promise<Response> {
  const base = getBackendBaseUrl()
  if (!base) {
    throw new Error('VITE_BACKEND_URL is not set. Add it to your .env file.')
  }

  const token = store.getState().user.accessToken
  const headers = new Headers(init.headers)

  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  if (init.body !== undefined && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  const res = await fetch(`${base.replace(/\/$/, '')}${path}`, {
    ...init,
    headers,
  })

  if (res.status === 401 && token) {
    store.dispatch(logout())
    window.location.assign('/login')
  }

  return res
}
