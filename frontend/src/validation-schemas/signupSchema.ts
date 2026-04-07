import { z } from 'zod'

export const userTypeSchema = z.enum(['student', 'staff', 'admin'])

export const signupSchema = z
  .object({
    fullName: z.string().min(1, 'Full name is required').max(200),
    email: z.string().min(1, 'Email is required').email('Enter a valid UL email'),
    password: z
      .string()
      .min(8, 'Password must be at least 8 characters')
      .max(128),
    confirmPassword: z.string().min(1, 'Confirm your password'),
    userType: userTypeSchema,
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: 'Passwords do not match',
    path: ['confirmPassword'],
  })

export type SignupFormValues = z.infer<typeof signupSchema>
