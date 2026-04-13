import { createBrowserRouter, Navigate } from 'react-router-dom'
import { APP_ROUTES } from '../constants/routes'
import { LoginPage } from '../views/pages/authentication/LoginPage'
import { RegisterPage } from '../views/pages/authentication/RegisterPage'
import { DashboardPage } from '../views/pages/dashboard/DashboardPage'
import { StudentDashboardPage } from '../views/pages/dashboard/StudentDashboardPage'
import { StudentProfilePage } from '../views/pages/dashboard/StudentProfilePage'
import { BrowseBooksPage } from '../views/pages/dashboard/BrowseBooksPage'
import { GuestRoute, RequireAuth, RootRedirect } from './AuthRoutes'

export const router = createBrowserRouter([
  { path: APP_ROUTES.HOME, element: <RootRedirect /> },
  {
    path: APP_ROUTES.LOGIN,
    element: (
      <GuestRoute>
        <LoginPage />
      </GuestRoute>
    ),
  },
  {
    path: APP_ROUTES.REGISTER,
    element: (
      <GuestRoute>
        <RegisterPage />
      </GuestRoute>
    ),
  },
  {
    path: APP_ROUTES.DASHBOARD,
    element: (
      <RequireAuth>
        <DashboardPage />
      </RequireAuth>
    ),
  },
  {
    path: APP_ROUTES.STUDENT_DASHBOARD,
    element: (
      <RequireAuth>
        <StudentDashboardPage />
      </RequireAuth>
    ),
  },
  {
    path: APP_ROUTES.STUDENT_PROFILE,
    element: (
      <RequireAuth>
        <StudentProfilePage />
      </RequireAuth>
    ),
  },
  {
    path: APP_ROUTES.BROWSE_BOOKS,
    element: (
      <RequireAuth>
        <BrowseBooksPage />
      </RequireAuth>
    ),
  },
  { path: '*', element: <Navigate to={APP_ROUTES.HOME} replace /> },
])
