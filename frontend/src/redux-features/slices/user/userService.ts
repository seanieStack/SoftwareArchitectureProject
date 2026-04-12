import { API_PATHS } from '../../../constants/api'
import { getBackendBaseUrl } from '../../../constants/env'
import type { LoginFormValues } from '../../../validation-schemas/loginSchema'
import type { SignupFormValues } from '../../../validation-schemas/signupSchema'
import { authResponseSchema, type AuthResponse } from './authTypes'

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

function parseAuthBody(data: unknown): AuthResponse {
  const parsed = authResponseSchema.safeParse(data)
  if (!parsed.success) {
    throw new Error('Invalid response from server')
  }
  return parsed.data
}

export async function loginRequest(
  payload: Pick<LoginFormValues, 'email' | 'password'>,
): Promise<AuthResponse> {
  const base = requireBaseUrl()
  const res = await fetch(`${base}${API_PATHS.LOGIN}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  if (!res.ok) {
    throw new Error(await parseErrorResponse(res))
  }
  const data: unknown = await res.json()
  return parseAuthBody(data)
}

export type RegisterRequestBody = Pick<
  SignupFormValues,
  'fullName' | 'email' | 'password'
>

export async function registerRequest(
  payload: RegisterRequestBody,
): Promise<AuthResponse> {
  const base = requireBaseUrl()
  const res = await fetch(`${base}${API_PATHS.REGISTER}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  if (!res.ok) {
    throw new Error(await parseErrorResponse(res))
  }
  const data: unknown = await res.json()
  return parseAuthBody(data)
}
