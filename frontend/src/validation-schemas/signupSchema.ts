import { z } from 'zod'

const registrationEmailSchema = z
  .string()
  .min(1, 'Email is required')
  .email('Enter a valid email address')
  .refine(
    (email) => {
      const e = email.toLowerCase()
      return e.endsWith('@studentmail.ul.ie') || e.endsWith('@ul.ie')
    },
    {
      message: 'Use a @studentmail.ul.ie (student) or @ul.ie (staff) email address',
    },
  )

export const signupSchema = z
  .object({
    fullName: z.string().min(1, 'Full name is required').max(200),
    email: registrationEmailSchema,
    password: z
      .string()
      .min(8, 'Password must be at least 8 characters')
      .max(128),
    confirmPassword: z.string().min(1, 'Confirm your password'),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: 'Passwords do not match',
    path: ['confirmPassword'],
  })

export type SignupFormValues = z.infer<typeof signupSchema>
