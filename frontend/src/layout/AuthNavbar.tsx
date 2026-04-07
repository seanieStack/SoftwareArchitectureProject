import { Link, NavLink } from 'react-router-dom'
import { APP_ROUTES } from '../constants/routes'
import { BookIcon } from '../views/components/utils/authIcons'

const navLinkClass = ({ isActive }: { isActive: boolean }) =>
  `text-sm font-medium transition hover:text-emerald-900 ${
    isActive ? 'text-emerald-800' : 'text-gray-700'
  }`

export function AuthNavbar() {
  return (
    <header className="border-b border-gray-200/80 bg-white">
      <div className="mx-auto flex max-w-6xl items-center justify-between gap-4 px-4 py-4 md:px-6">
        <Link
          to={APP_ROUTES.LOGIN}
          className="flex min-w-0 items-center gap-2 md:gap-3"
        >
          <img
            src="/ul-logo.png"
            alt=""
            className="h-9 w-auto shrink-0 object-contain md:h-10"
            width={120}
            height={40}
          />
        </Link>

        <nav className="flex shrink-0 items-center gap-3 md:gap-4">
          <NavLink to={APP_ROUTES.LOGIN} className={navLinkClass}>
            Login
          </NavLink>
          <Link
            to={APP_ROUTES.REGISTER}
            className="rounded-lg bg-emerald-800 px-4 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-emerald-900"
          >
            Register
          </Link>
        </nav>
      </div>
    </header>
  )
}
