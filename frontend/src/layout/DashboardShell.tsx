import type { ReactNode } from 'react'
import { Link, NavLink } from 'react-router-dom'
import { APP_ROUTES } from '../constants/routes'
import { BookIcon } from '../views/components/utils/authIcons'

const sidebarLinkClass = ({ isActive }: { isActive: boolean }) =>
  `block rounded-lg px-3 py-2 text-sm font-medium transition ${
    isActive
      ? 'bg-emerald-50 text-emerald-900'
      : 'text-gray-700 hover:bg-gray-100 hover:text-emerald-900'
  }`

export type DashboardNavItem = {
  to: string
  label: string
  end?: boolean
}

type DashboardShellProps = {
  roleLabel: string
  navItems: DashboardNavItem[]
  children: ReactNode
}

export function DashboardShell({ roleLabel, navItems, children }: DashboardShellProps) {
  return (
    <div className="flex min-h-screen flex-col bg-gray-50">
      <header className="border-b border-gray-200/80 bg-white">
        <div className="mx-auto flex max-w-7xl items-center justify-between gap-4 px-4 py-4 md:px-6">
          <Link
            to={APP_ROUTES.HOME}
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
          <div className="flex items-center gap-3">
            <span className="hidden text-sm text-gray-500 sm:inline">{roleLabel}</span>
            <span
              className="inline-flex items-center gap-1.5 rounded-full border border-emerald-200 bg-emerald-50 px-3 py-1 text-xs font-semibold text-emerald-900"
              title="Role"
            >
              <BookIcon />
              E-Library
            </span>
          </div>
        </div>
      </header>

      <div className="mx-auto flex w-full max-w-7xl flex-1 flex-col gap-0 md:flex-row md:gap-6 md:px-6">
        <aside className="shrink-0 border-b border-gray-200/80 bg-white px-3 py-3 md:w-56 md:border-0 md:border-r md:border-gray-200/80 md:bg-transparent md:px-0 md:py-8">
          <nav
            className="flex flex-wrap gap-1 md:flex-col md:gap-1"
            aria-label="Dashboard"
          >
            {navItems.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.end}
                className={sidebarLinkClass}
              >
                {item.label}
              </NavLink>
            ))}
          </nav>
        </aside>

        <main className="min-w-0 flex-1 px-4 py-6 md:px-0 md:py-10">{children}</main>
      </div>
    </div>
  )
}
