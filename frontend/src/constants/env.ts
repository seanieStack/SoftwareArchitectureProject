/** Backend / API gateway base URL from Vite env (see `.env.example`). */
export function getBackendBaseUrl(): string {
  const url = import.meta.env.VITE_BACKEND_URL
  if (typeof url !== 'string' || url.trim() === '') {
    return ''
  }
  return url.replace(/\/$/, '')
}
