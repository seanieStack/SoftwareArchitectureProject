import { createBrowserRouter, Navigate } from 'react-router-dom'
import { APP_ROUTES } from '../constants/routes'
import { LoginPage } from '../views/pages/authentication/LoginPage'
import { RegisterPage } from '../views/pages/authentication/RegisterPage'

export const router = createBrowserRouter([
  { path: APP_ROUTES.HOME, element: <Navigate to={APP_ROUTES.LOGIN} replace /> },
  { path: APP_ROUTES.LOGIN, element: <LoginPage /> },
  { path: APP_ROUTES.REGISTER, element: <RegisterPage /> },
  { path: '*', element: <Navigate to={APP_ROUTES.LOGIN} replace /> },
])
