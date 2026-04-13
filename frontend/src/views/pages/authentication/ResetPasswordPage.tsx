import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { resetPasswordRequest } from '../../../redux-features/slices/user/userService'
import {
  resetPasswordSchema,
  type ResetPasswordFormValues,
} from '../../../validation-schemas/resetPasswordSchema'
import { AuthPageShell } from '../../../layout/AuthPageShell'
import { PrimaryButton } from '../../components/buttons/PrimaryButton'
import { AuthCard } from '../../components/utils/AuthCard'
import { AuthHeader } from '../../components/utils/AuthHeader'
import { LockIcon } from '../../components/utils/authIcons'
import { IconInput } from '../../components/utils/IconInput'
import { APP_ROUTES } from '../../../constants/routes'

export function ResetPasswordPage() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const token = searchParams.get('token')?.trim() ?? ''

  const [status, setStatus] = useState<'idle' | 'loading' | 'success' | 'error'>('idle')
  const [message, setMessage] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ResetPasswordFormValues>({
    resolver: zodResolver(resetPasswordSchema),
    defaultValues: { password: '', confirmPassword: '' },
  })

  const onSubmit = async (values: ResetPasswordFormValues) => {
    if (!token) return
    setStatus('loading')
    setMessage(null)
    try {
      const msg = await resetPasswordRequest(token, values.password)
      setMessage(msg)
      setStatus('success')
      window.setTimeout(() => {
        navigate(APP_ROUTES.LOGIN, { replace: true })
      }, 2000)
    } catch (e) {
      setMessage(e instanceof Error ? e.message : 'Something went wrong')
      setStatus('error')
    }
  }

  if (!token) {
    return (
      <AuthPageShell>
        <AuthCard>
          <AuthHeader
            title="Invalid link"
            subtitle="This password reset link is missing a token or has expired. Request a new one from the sign-in page."
            align="center"
          />
          <p className="text-center text-sm text-gray-600">
            <Link
              to={APP_ROUTES.FORGOT_PASSWORD}
              className="font-semibold text-emerald-800 hover:text-emerald-900"
            >
              Request a new link
            </Link>
          </p>
        </AuthCard>
      </AuthPageShell>
    )
  }

  return (
    <AuthPageShell>
      <AuthCard>
        <AuthHeader
          title="Set a new password"
          subtitle="Choose a strong password you haven’t used elsewhere."
          align="center"
        />

        {status === 'success' && message ? (
          <div
            className="mb-6 rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-900"
            role="status"
          >
            {message} Redirecting to sign in…
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
              id="reset-password"
              label="New password"
              requiredMark
              type="password"
              autoComplete="new-password"
              placeholder="At least 8 characters"
              icon={<LockIcon />}
              error={errors.password?.message}
              {...register('password')}
            />

            <IconInput
              id="reset-confirm-password"
              label="Confirm new password"
              requiredMark
              type="password"
              autoComplete="new-password"
              placeholder="Repeat your password"
              icon={<LockIcon />}
              error={errors.confirmPassword?.message}
              {...register('confirmPassword')}
            />

            <PrimaryButton loading={status === 'loading'}>Update password</PrimaryButton>
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
