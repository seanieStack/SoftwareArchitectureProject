import type { ReactNode } from 'react'
import { Navigate } from 'react-router-dom'
import { APP_ROUTES } from '../constants/routes'
import { useAppSelector } from '../redux-features/store/hooks'

export function RootRedirect() {
  const accessToken = useAppSelector((s) => s.user.accessToken)
  return (
    <Navigate
      to={accessToken ? APP_ROUTES.DASHBOARD : APP_ROUTES.LOGIN}
      replace
    />
  )
}

export function GuestRoute({ children }: { children: ReactNode }) {
  const accessToken = useAppSelector((s) => s.user.accessToken)
  if (accessToken) {
    return <Navigate to={APP_ROUTES.DASHBOARD} replace />
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
