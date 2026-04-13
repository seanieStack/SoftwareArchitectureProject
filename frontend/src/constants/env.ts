/**
 * API gateway base URL.
 *
 * - Leave `VITE_BACKEND_URL` empty: the Vite dev server proxies `/api` → gateway (same origin, no CORS).
 * - Set it to the full gateway URL (e.g. `http://localhost:8080`) for `vite preview` or static hosting without a proxy.
 */
export function getBackendBaseUrl(): string {
  const url = import.meta.env.VITE_BACKEND_URL
  if (typeof url !== 'string' || url.trim() === '') {
    return ''
  }
  return url.replace(/\/$/, '')
}
