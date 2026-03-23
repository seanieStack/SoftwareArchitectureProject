import { useMemo, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAppDispatch, useAppSelector } from '../../../redux-features/store/hooks'
import { clearSession } from '../../../redux-features/slices/user/userSlice'
import { APP_ROUTES } from '../../../constants/routes'
import { UL_LOGO_SRC, WEBSITE_LOGO_SRC } from '../../../constants/assets'
import { DashboardUserMenu } from '../../components/dashboard/DashboardUserMenu'

type DashboardSection = 'borrowed' | 'history' | 'notifications' | 'fines'

const SECTIONS: { id: DashboardSection; label: string }[] = [
  { id: 'borrowed', label: 'Borrowed Books' },
  { id: 'history', label: 'Borrow History' },
  { id: 'notifications', label: 'Notifications' },
  { id: 'fines', label: 'Fines' },
]

function displayNameFromProfile(profile: unknown): string {
  if (profile && typeof profile === 'object') {
    const p = profile as Record<string, unknown>
    if (typeof p.fullName === 'string' && p.fullName.trim()) return p.fullName.trim()
    if (typeof p.name === 'string' && p.name.trim()) return p.name.trim()
    if (typeof p.email === 'string' && p.email.trim()) {
      const local = p.email.split('@')[0]
      if (local) return local.replace(/[._]/g, ' ')
    }
  }
  return 'Student'
}

function CompassIcon({ className }: { className?: string }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" aria-hidden>
      <circle cx="12" cy="12" r="9" stroke="currentColor" strokeWidth="1.75" />
      <path
        d="M14.5 9.5l-2 5-5 2 2-5 5-2z"
        stroke="currentColor"
        strokeWidth="1.75"
        strokeLinejoin="round"
      />
    </svg>
  )
}

function SectionPanel({ section }: { section: DashboardSection }) {
  const copy: Record<DashboardSection, string> = {
    borrowed: 'No borrowed books.',
    history: 'No borrow history.',
    notifications: 'No notifications.',
    fines: 'No fines.',
  }
  return <p className="text-sm text-gray-500">{copy[section]}</p>
}

export function StudentDashboardPage() {
  const dispatch = useAppDispatch()
  const navigate = useNavigate()
  const profile = useAppSelector((s) => s.user.profile)
  const [section, setSection] = useState<DashboardSection>('borrowed')

  const displayName = useMemo(() => displayNameFromProfile(profile), [profile])

  const onLogout = () => {
    dispatch(clearSession())
    navigate(APP_ROUTES.LOGIN, { replace: true })
  }

  return (
    <div className="min-h-screen bg-[#f6f7f9]">
      <header className="sticky top-0 z-20 border-b border-gray-200/90 bg-white/95 backdrop-blur">
        <div className="flex w-full items-center justify-between gap-4 px-4 py-2 sm:px-6 lg:px-8">
          <Link
            to={APP_ROUTES.STUDENT_DASHBOARD}
            className="shrink-0 rounded-lg outline-none ring-emerald-200 focus-visible:ring-2"
          >
            <img
              src={UL_LOGO_SRC}
              alt="University of Limerick"
              className="h-9 w-auto max-h-10 object-contain sm:h-10"
              width={120}
              height={40}
            />
          </Link>
          <DashboardUserMenu displayName={displayName} onLogout={onLogout} />
        </div>
      </header>

      <div className="flex w-full flex-col gap-8 px-4 py-8 sm:px-6 md:flex-row md:gap-0 md:py-10 lg:px-8">
        <aside className="md:w-64 md:shrink-0 md:pr-8 lg:w-72">
          <div className="mb-6 rounded-2xl border border-emerald-200/80 bg-gradient-to-br from-emerald-50 to-white p-4 shadow-sm">
            <p className="text-xs font-medium uppercase tracking-wide text-emerald-800/80">
              Next step
            </p>
            <p className="mt-1 text-sm text-gray-700">Find your next read in the catalog.</p>
            <Link
              to={APP_ROUTES.BROWSE_BOOKS}
              className="mt-4 flex items-center justify-center gap-2 rounded-xl bg-emerald-800 px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition hover:bg-emerald-900"
            >
              <CompassIcon className="size-4" />
              Browse books
            </Link>
          </div>

          <nav className="space-y-1" aria-label="Dashboard sections">
            {SECTIONS.map((item) => {
              const active = section === item.id
              return (
                <button
                  key={item.id}
                  type="button"
                  onClick={() => setSection(item.id)}
                  className={`flex w-full items-center gap-2 rounded-xl px-3 py-2.5 text-left text-sm font-medium transition ${
                    active
                      ? 'bg-white text-emerald-900 shadow-md ring-1 ring-emerald-200/60'
                      : 'text-gray-600 hover:bg-white/70 hover:text-gray-900'
                  }`}
                >
                  <span
                    className={`h-8 w-1 shrink-0 rounded-full transition ${
                      active ? 'bg-emerald-600' : 'bg-transparent'
                    }`}
                    aria-hidden
                  />
                  {item.label}
                </button>
              )
            })}
          </nav>
        </aside>

        <main className="min-w-0 flex-1">
          <div className="mb-6 overflow-hidden rounded-3xl border border-emerald-200/50 bg-gradient-to-br from-emerald-50/90 via-white to-white px-5 py-5 shadow-sm sm:px-6 sm:py-6">
            <div className="flex flex-col gap-5 sm:flex-row sm:items-center sm:gap-8">
              <div className="order-1 min-w-0 flex-1 sm:order-3">
                <h1 className="text-2xl font-bold tracking-tight text-gray-900 md:text-3xl">
                  My Dashboard
                </h1>
                <p className="mt-1 text-gray-600">
                  Welcome back, <span className="font-medium text-gray-800">{displayName}</span>.
                </p>
              </div>

              <div
                className="order-2 hidden h-16 w-px shrink-0 bg-gradient-to-b from-transparent via-emerald-200/70 to-transparent sm:order-2 sm:block"
                aria-hidden
              />

              <div className="relative order-3 shrink-0 sm:order-1 sm:max-w-[min(100%,220px)]">
                <div
                  className="absolute -inset-1 rounded-2xl bg-gradient-to-br from-emerald-200/40 to-emerald-600/10 blur-sm"
                  aria-hidden
                />
                <div className="relative rounded-2xl border border-white/80 bg-white/95 p-3 shadow-md ring-1 ring-emerald-100/80">
                  <img
                    src={WEBSITE_LOGO_SRC}
                    alt="UL E-Library"
                    className="mx-auto h-11 w-auto max-w-full object-contain sm:h-12"
                    width={200}
                    height={48}
                  />
                  <p className="mt-2 text-center text-[10px] font-semibold uppercase tracking-[0.2em] text-emerald-800/70">
                    E-Library
                  </p>
                </div>
              </div>
            </div>
          </div>

          <div className="rounded-2xl border border-gray-200/80 bg-white p-5 shadow-md md:p-6">
            <h2 className="text-lg font-semibold text-gray-900">
              {SECTIONS.find((s) => s.id === section)?.label}
            </h2>
            <div className="mt-5">
              <SectionPanel section={section} />
            </div>
          </div>
        </main>
      </div>
    </div>
  )
}
