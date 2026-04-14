import { createBrowserRouter, Navigate, useRouteError } from 'react-router-dom'
import { APP_ROUTES } from '../constants/routes'
import { ForgotPasswordPage } from '../views/pages/authentication/ForgotPasswordPage'
import { LoginPage } from '../views/pages/authentication/LoginPage'
import { RegisterPage } from '../views/pages/authentication/RegisterPage'
import { ResetPasswordPage } from '../views/pages/authentication/ResetPasswordPage'
import { DashboardPage } from '../views/pages/dashboard/DashboardPage'
import { StudentDashboardPage } from '../views/pages/dashboard/StudentDashboardPage'
import { StudentProfilePage } from '../views/pages/dashboard/StudentProfilePage'
import { BrowseBooksPage } from '../views/pages/dashboard/BrowseBooksPage'
import { AdminDashboard } from '../views/pages/AdminDashboard'
import { AdminAnalyticsPage } from '../views/pages/admin/AdminAnalyticsPage'
import { AdminUserManagementPage } from '../views/pages/admin/AdminUserManagementPage'
import { BookManagementPage } from '../views/pages/admin/BookManagementPage'
import { GuestRoute, RequireAdmin, RequireAuth, RootRedirect } from './AuthRoutes'

function RouteError() {
  const error = useRouteError()
  const message =
    error instanceof Error
      ? error.message
      : typeof error === 'string'
        ? error
        : JSON.stringify(error)
  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-4 bg-gray-50 p-8 text-center">
      <p className="text-lg font-semibold text-gray-900">Something went wrong</p>
      <pre className="max-w-xl overflow-auto rounded-lg border border-red-200 bg-red-50 p-4 text-left text-sm text-red-700">
        {message}
      </pre>
      <a href="/" className="text-sm text-emerald-800 underline">
        Go home
      </a>
    </div>
  )
}

export const router = createBrowserRouter([
  {
    path: '/',
    errorElement: <RouteError />,
    children: [
      { index: true, element: <RootRedirect /> },
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
      { path: APP_ROUTES.FORGOT_PASSWORD, element: <ForgotPasswordPage /> },
      { path: APP_ROUTES.RESET_PASSWORD, element: <ResetPasswordPage /> },
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
      {
        path: APP_ROUTES.ADMIN_DASHBOARD,
        element: (
          <RequireAdmin>
            <AdminDashboard />
          </RequireAdmin>
        ),
      },
      {
        path: APP_ROUTES.ADMIN_BOOKS,
        element: (
          <RequireAdmin>
            <BookManagementPage />
          </RequireAdmin>
        ),
      },
      {
        path: APP_ROUTES.ADMIN_USERS,
        element: (
          <RequireAdmin>
            <AdminUserManagementPage />
          </RequireAdmin>
        ),
      },
      {
        path: APP_ROUTES.ADMIN_ANALYTICS,
        element: (
          <RequireAdmin>
            <AdminAnalyticsPage />
          </RequireAdmin>
        ),
      },
      { path: '*', element: <Navigate to={APP_ROUTES.HOME} replace /> },
    ],
  },
])
