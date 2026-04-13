import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link } from 'react-router-dom'
import { forgotPasswordRequest } from '../../../redux-features/slices/user/userService'
import {
  forgotPasswordSchema,
  type ForgotPasswordFormValues,
} from '../../../validation-schemas/forgotPasswordSchema'
import { AuthPageShell } from '../../../layout/AuthPageShell'
import { PrimaryButton } from '../../components/buttons/PrimaryButton'
import { AuthCard } from '../../components/utils/AuthCard'
import { AuthHeader } from '../../components/utils/AuthHeader'
import { MailIcon } from '../../components/utils/authIcons'
import { IconInput } from '../../components/utils/IconInput'
import { APP_ROUTES } from '../../../constants/routes'

export function ForgotPasswordPage() {
  const [status, setStatus] = useState<'idle' | 'loading' | 'success' | 'error'>('idle')
  const [message, setMessage] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ForgotPasswordFormValues>({
    resolver: zodResolver(forgotPasswordSchema),
    defaultValues: { email: '' },
  })

  const onSubmit = async (values: ForgotPasswordFormValues) => {
    setStatus('loading')
    setMessage(null)
    try {
      const msg = await forgotPasswordRequest(values.email)
      setMessage(msg)
      setStatus('success')
    } catch (e) {
      setMessage(e instanceof Error ? e.message : 'Something went wrong')
      setStatus('error')
    }
  }

  return (
    <AuthPageShell>
      <AuthCard>
        <AuthHeader
          title="Forgot password"
          subtitle="Enter your account email and we’ll send you a link to reset your password."
          align="center"
        />

        {status === 'success' && message ? (
          <div
            className="mb-6 rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-900"
            role="status"
          >
            {message}
          </div>
        ) : null}

        {status === 'error' && message ? (
          <div
            className="mb-6 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800"
            role="alert"
          >
            {message}
          </div>
        ) : null}

        {status !== 'success' ? (
          <form className="space-y-5" onSubmit={handleSubmit(onSubmit)} noValidate>
            <IconInput
              id="forgot-email"
              label="Email Address"
              requiredMark
              type="email"
              autoComplete="email"
              placeholder="Enter your email address"
              icon={<MailIcon />}
              error={errors.email?.message}
              {...register('email')}
            />

            <PrimaryButton loading={status === 'loading'}>Send reset link</PrimaryButton>
          </form>
        ) : null}

        <p className="mt-8 text-center text-sm text-gray-600">
          <Link
            to={APP_ROUTES.LOGIN}
            className="font-semibold text-emerald-800 hover:text-emerald-900"
          >
            Back to sign in
          </Link>
        </p>
      </AuthCard>
    </AuthPageShell>
  )
}
