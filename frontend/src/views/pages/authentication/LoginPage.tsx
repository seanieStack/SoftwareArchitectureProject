import { zodResolver } from '@hookform/resolvers/zod'
import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { useNavigate } from 'react-router-dom'
import { useAppDispatch, useAppSelector } from '../../../redux-features/store/hooks'
import {
  clearLoginError,
  loginUser,
} from '../../../redux-features/slices/user/userSlice'
import { loginSchema, type LoginFormValues } from '../../../validation-schemas/loginSchema'
import { AuthPageShell } from '../../../layout/AuthPageShell'
import { PrimaryButton } from '../../components/buttons/PrimaryButton'
import { AuthCard } from '../../components/utils/AuthCard'
import { AuthFormFooter } from '../../components/utils/AuthFormFooter'
import { AuthHeader } from '../../components/utils/AuthHeader'
import { LockIcon, MailIcon } from '../../components/utils/authIcons'
import { IconInput } from '../../components/utils/IconInput'
import { APP_ROUTES } from '../../../constants/routes'

export function LoginPage() {
  const dispatch = useAppDispatch()
  const navigate = useNavigate()
  const loginStatus = useAppSelector((s) => s.user.loginStatus)
  const loginError = useAppSelector((s) => s.user.loginError)
  const [rememberMe, setRememberMe] = useState(false)

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: '', password: '' },
  })

  useEffect(() => {
    dispatch(clearLoginError())
  }, [dispatch])

  useEffect(() => {
    if (loginStatus === 'succeeded') {
      navigate(APP_ROUTES.STUDENT_DASHBOARD, { replace: true })
    }
  }, [loginStatus, navigate])

  const onSubmit = (values: LoginFormValues) => {
    void dispatch(loginUser({ ...values, rememberMe }))
  }

  return (
    <AuthPageShell>
      <AuthCard>
        <AuthHeader
          title="Welcome Back"
          subtitle="Sign in to access your E-Library account"
          align="center"
        />

        {loginStatus === 'failed' && loginError ? (
          <div
            className="mb-6 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800"
            role="alert"
          >
            {loginError}
          </div>
        ) : null}

        <form className="space-y-5" onSubmit={handleSubmit(onSubmit)} noValidate>
          <IconInput
            id="login-email"
            label="Email Address"
            requiredMark
            type="email"
            autoComplete="email"
            placeholder="Enter your email address"
            icon={<MailIcon />}
            error={errors.email?.message}
            {...register('email')}
          />

          <IconInput
            id="login-password"
            label="Password"
            requiredMark
            type="password"
            autoComplete="current-password"
            placeholder="Enter your password"
            icon={<LockIcon />}
            error={errors.password?.message}
            {...register('password')}
          />

          <div className="flex flex-wrap items-center justify-between gap-3 text-sm">
            <label className="flex cursor-pointer items-center gap-2 text-gray-700">
              <input
                type="checkbox"
                checked={rememberMe}
                onChange={(e) => setRememberMe(e.target.checked)}
                className="size-4 rounded border-gray-300 text-emerald-800 focus:ring-emerald-800"
              />
              Remember me
            </label>
            <a
              href="#"
              className="font-medium text-gray-500 hover:text-emerald-800"
              onClick={(e) => e.preventDefault()}
            >
              Forgot password?
            </a>
          </div>

          <PrimaryButton loading={loginStatus === 'loading'}>Sign In</PrimaryButton>
        </form>

        <AuthFormFooter variant="login" />
      </AuthCard>
    </AuthPageShell>
  )
}
