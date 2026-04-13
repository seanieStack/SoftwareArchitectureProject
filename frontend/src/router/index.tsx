import { createBrowserRouter, Navigate } from 'react-router-dom'
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
])
