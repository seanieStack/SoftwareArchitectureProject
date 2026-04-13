import { z } from 'zod'

/** Matches core-service `UserResponse` + auth envelope. */
export const userProfileSchema = z.object({
  id: z.number(),
  email: z.string(),
  fullName: z.string(),
  userType: z.enum(['student', 'staff', 'admin']),
})

export type UserProfile = z.infer<typeof userProfileSchema>

export const authResponseSchema = z.object({
  accessToken: z.string(),
  tokenType: z.string(),
  expiresIn: z.number(),
  user: userProfileSchema,
})

export type AuthResponse = z.infer<typeof authResponseSchema>

export function parseAuthResponse(data: unknown): AuthResponse {
  return authResponseSchema.parse(data)
}
