import { API_PATHS } from '../../../constants/api'
import { getBackendBaseUrl } from '../../../constants/env'
import type { LoginFormValues } from '../../../validation-schemas/loginSchema'
import type { SignupFormValues } from '../../../validation-schemas/signupSchema'
import { authResponseSchema, type AuthResponse } from './authTypes'

function apiBase(): string {
  return getBackendBaseUrl()
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
  const base = apiBase()
  const res = await fetch(`${base}${API_PATHS.AUTH.LOGIN}`, {
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
  const base = apiBase()
  const res = await fetch(`${base}${API_PATHS.AUTH.REGISTER}`, {
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

export async function refreshRequest(refreshToken: string): Promise<AuthResponse> {
  const base = apiBase()
  const res = await fetch(`${base}${API_PATHS.AUTH.REFRESH}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refreshToken }),
  })
  if (!res.ok) {
    throw new Error(await parseErrorResponse(res))
  }
  const data: unknown = await res.json()
  return parseAuthBody(data)
}

async function parseMessageResponse(res: Response): Promise<string> {
  const text = await res.text()
  if (!text) return ''
  try {
    const body = JSON.parse(text) as { message?: string }
    return body.message ?? text
  } catch {
    return text
  }
}

export async function forgotPasswordRequest(email: string): Promise<string> {
  const base = apiBase()
  const res = await fetch(`${base}${API_PATHS.AUTH.FORGOT_PASSWORD}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email }),
  })
  if (!res.ok) {
    throw new Error(await parseErrorResponse(res))
  }
  return parseMessageResponse(res)
}

export async function resetPasswordRequest(token: string, newPassword: string): Promise<string> {
  const base = apiBase()
  const res = await fetch(`${base}${API_PATHS.AUTH.RESET_PASSWORD}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ token, newPassword }),
  })
  if (!res.ok) {
    throw new Error(await parseErrorResponse(res))
  }
  return parseMessageResponse(res)
}
