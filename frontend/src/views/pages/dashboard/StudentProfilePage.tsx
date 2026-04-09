import { Link } from 'react-router-dom'
import { APP_ROUTES } from '../../../constants/routes'
import { WEBSITE_LOGO_SRC } from '../../../constants/assets'

export function StudentProfilePage() {
  return (
    <div className="min-h-screen bg-[#f6f7f9]">
      <header className="border-b border-gray-200/90 bg-white">
        <div className="flex w-full items-center justify-between gap-4 px-4 py-3.5 sm:px-6 lg:px-8">
          <img
            src={WEBSITE_LOGO_SRC}
            alt="UL E-Library"
            className="h-9 w-auto object-contain md:h-10"
            width={160}
            height={40}
          />
          <Link
            to={APP_ROUTES.STUDENT_DASHBOARD}
            className="text-sm font-semibold text-emerald-800 hover:text-emerald-900"
          >
            ← Back to dashboard
          </Link>
        </div>
      </header>

      <main className="w-full px-4 py-10 sm:px-6 lg:px-8">
        <h1 className="text-2xl font-bold text-gray-900 md:text-3xl">User profile</h1>
        <p className="mt-2 max-w-xl text-gray-600">
          Profile details and preferences will appear here when connected to your account API.
        </p>
      </main>
    </div>
  )
}
