import { zodResolver } from '@hookform/resolvers/zod'
import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { useAppDispatch, useAppSelector } from '../../../redux-features/store/hooks'
import {
  clearRegisterError,
  registerUser,
} from '../../../redux-features/slices/user/userSlice'
import {
  signupSchema,
  type SignupFormValues,
} from '../../../validation-schemas/signupSchema'
import { AuthPageShell } from '../../../layout/AuthPageShell'
import { PrimaryButton } from '../../components/buttons/PrimaryButton'
import { AuthCard } from '../../components/utils/AuthCard'
import { AuthFormFooter } from '../../components/utils/AuthFormFooter'
import { AuthHeader } from '../../components/utils/AuthHeader'
import { LockIcon, MailIcon, UserIcon } from '../../components/utils/authIcons'
import { IconInput } from '../../components/utils/IconInput'
export function RegisterPage() {
  const dispatch = useAppDispatch()
  const registerStatus = useAppSelector((s) => s.user.registerStatus)
  const registerError = useAppSelector((s) => s.user.registerError)

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<SignupFormValues>({
    resolver: zodResolver(signupSchema),
    defaultValues: {
      fullName: '',
      email: '',
      password: '',
      confirmPassword: '',
    },
  })

  useEffect(() => {
    dispatch(clearRegisterError())
  }, [dispatch])

  const onSubmit = (values: SignupFormValues) => {
    const { fullName, email, password } = values
    void dispatch(registerUser({ fullName, email, password }))
  }

  return (
    <AuthPageShell>
      <AuthCard>
        <AuthHeader
          align="center"
          title="Create Account"
          subtitle="Join our E-Library community today"
        />

        {registerStatus === 'failed' && registerError ? (
          <div
            className="mb-6 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800"
            role="alert"
          >
            {registerError}
          </div>
        ) : null}

        <form className="space-y-5" onSubmit={handleSubmit(onSubmit)} noValidate>
          <div className="grid gap-5 md:grid-cols-2">
            <IconInput
              id="register-fullName"
              label="Full Name"
              requiredMark
              autoComplete="name"
              placeholder="Enter your name"
              icon={<UserIcon />}
              error={errors.fullName?.message}
              {...register('fullName')}
            />
            <IconInput
              id="register-email"
              label="Email Address"
              requiredMark
              type="email"
              autoComplete="email"
              placeholder="Enter your email address"
              icon={<MailIcon />}
              error={errors.email?.message}
              {...register('email')}
            />
          </div>

          <div className="grid gap-5 md:grid-cols-2">
            <IconInput
              id="register-password"
              label="Password"
              requiredMark
              type="password"
              autoComplete="new-password"
              placeholder="Create a password"
              icon={<LockIcon />}
              error={errors.password?.message}
              {...register('password')}
            />
            <IconInput
              id="register-confirmPassword"
              label="Confirm Password"
              requiredMark
              type="password"
              autoComplete="new-password"
              placeholder="Confirm your password"
              icon={<LockIcon />}
              error={errors.confirmPassword?.message}
              {...register('confirmPassword')}
            />
          </div>

          <PrimaryButton loading={registerStatus === 'loading'}>
            Create Account
          </PrimaryButton>
        </form>

        <AuthFormFooter variant="register" />
      </AuthCard>
    </AuthPageShell>
  )
}
