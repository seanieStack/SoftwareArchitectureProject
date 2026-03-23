import { createBrowserRouter, Navigate } from 'react-router-dom'
import { APP_ROUTES } from '../constants/routes'
import { LoginPage } from '../views/pages/authentication/LoginPage'
import { RegisterPage } from '../views/pages/authentication/RegisterPage'
import { StudentDashboardPage } from '../views/pages/dashboard/StudentDashboardPage'
import { StudentProfilePage } from '../views/pages/dashboard/StudentProfilePage'
import { BrowseBooksPage } from '../views/pages/dashboard/BrowseBooksPage'

export const router = createBrowserRouter([
  { path: APP_ROUTES.HOME, element: <Navigate to={APP_ROUTES.LOGIN} replace /> },
  { path: APP_ROUTES.LOGIN, element: <LoginPage /> },
  { path: APP_ROUTES.REGISTER, element: <RegisterPage /> },
  { path: APP_ROUTES.STUDENT_DASHBOARD, element: <StudentDashboardPage /> },
  { path: APP_ROUTES.STUDENT_PROFILE, element: <StudentProfilePage /> },
  { path: APP_ROUTES.BROWSE_BOOKS, element: <BrowseBooksPage /> },
  { path: '*', element: <Navigate to={APP_ROUTES.LOGIN} replace /> },
])
