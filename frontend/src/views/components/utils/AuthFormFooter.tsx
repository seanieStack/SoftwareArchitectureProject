import { Link } from 'react-router-dom'
import { APP_ROUTES } from '../../../constants/routes'

type AuthFormFooterProps = {
  variant: 'login' | 'register'
}

export function AuthFormFooter({ variant }: AuthFormFooterProps) {
  if (variant === 'login') {
    return (
      <div className="mt-8 space-y-6 text-center text-sm text-gray-600">
        <p>
          Don&apos;t have an account?{' '}
          <Link
            to={APP_ROUTES.REGISTER}
            className="font-semibold text-emerald-800 hover:text-emerald-900"
          >
            Register here
          </Link>
        </p>
        <div className="border-t border-gray-200 pt-6">
          <p className="text-xs leading-relaxed text-gray-500">
            Use your UL email address to access the system 
          </p>
        </div>
      </div>
    )
  }

  return (
    <p className="mt-8 text-center text-sm text-gray-600">
      Already have an account?{' '}
      <Link
        to={APP_ROUTES.LOGIN}
        className="font-semibold text-emerald-800 hover:text-emerald-900"
      >
        Login here
      </Link>
    </p>
  )
}
