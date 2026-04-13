import type { ReactNode } from 'react'
import { Navigate } from 'react-router-dom'
import { APP_ROUTES } from '../constants/routes'
import { useAppSelector } from '../redux-features/store/hooks'

function homeForRole(userType: string | undefined): string {
  return userType === 'admin' ? APP_ROUTES.ADMIN_DASHBOARD : APP_ROUTES.STUDENT_DASHBOARD
}

export function RootRedirect() {
  const accessToken = useAppSelector((s) => s.user.accessToken)
  const userType = useAppSelector((s) => s.user.profile?.userType)
  return (
    <Navigate
      to={accessToken ? homeForRole(userType) : APP_ROUTES.LOGIN}
      replace
    />
  )
}

export function GuestRoute({ children }: { children: ReactNode }) {
  const accessToken = useAppSelector((s) => s.user.accessToken)
  const userType = useAppSelector((s) => s.user.profile?.userType)
  if (accessToken) {
    return <Navigate to={homeForRole(userType)} replace />
  }
  return children
}

export function RequireAuth({ children }: { children: ReactNode }) {
  const accessToken = useAppSelector((s) => s.user.accessToken)
  if (!accessToken) {
    return <Navigate to={APP_ROUTES.LOGIN} replace />
  }
  return children
}

export function RequireAdmin({ children }: { children: ReactNode }) {
  const accessToken = useAppSelector((s) => s.user.accessToken)
  const userType = useAppSelector((s) => s.user.profile?.userType)
  if (!accessToken) {
    return <Navigate to={APP_ROUTES.LOGIN} replace />
  }
  if (userType !== 'admin') {
    return <Navigate to={APP_ROUTES.STUDENT_DASHBOARD} replace />
  }
  return children
}
