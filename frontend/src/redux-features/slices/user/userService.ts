import { API_PATHS } from '../../../constants/api'
import { getBackendBaseUrl } from '../../../constants/env'
import type { LoginFormValues } from '../../../validation-schemas/loginSchema'
import type { SignupFormValues } from '../../../validation-schemas/signupSchema'

function requireBaseUrl(): string {
  const base = getBackendBaseUrl()
  if (!base) {
    throw new Error('VITE_BACKEND_URL is not set. Add it to your .env file.')
  }
  return base
}

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

export async function loginRequest(
  payload: Pick<LoginFormValues, 'email' | 'password'>,
): Promise<unknown> {
  const base = requireBaseUrl()
  const res = await fetch(`${base}${API_PATHS.LOGIN}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  if (!res.ok) {
    throw new Error(await parseErrorResponse(res))
  }
  return res.json() as Promise<unknown>
}

export type RegisterRequestBody = Pick<
  SignupFormValues,
  'fullName' | 'email' | 'password' | 'userType'
>

export async function registerRequest(
  payload: RegisterRequestBody,
): Promise<unknown> {
  const base = requireBaseUrl()
  const res = await fetch(`${base}${API_PATHS.REGISTER}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  if (!res.ok) {
    throw new Error(await parseErrorResponse(res))
  }
  return res.json() as Promise<unknown>
}
