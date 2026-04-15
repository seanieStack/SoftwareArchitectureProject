import { useEffect, useMemo, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAppDispatch, useAppSelector } from '../../../redux-features/store/hooks'
import { logout } from '../../../redux-features/slices/user/userSlice'
import { APP_ROUTES } from '../../../constants/routes'
import { UL_LOGO_SRC, WEBSITE_LOGO_SRC } from '../../../constants/assets'
import { DashboardUserMenu } from '../../components/dashboard/DashboardUserMenu'
import {
  getBorrowsForUser,
  returnBorrow,
  getFinesForUser,
  acknowledgeFine,
  payFine,
  getNotificationsForUser,
  markNotificationRead,
} from '../../../http/supportService'
import type { Borrow, Fine, Notification } from '../../../types/support'

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

function fmtDate(iso: string) {
  return new Date(iso).toLocaleDateString('en-IE', { day: 'numeric', month: 'short', year: 'numeric' })
}

// ── Borrowed Books section ────────────────────────────────────────────────────

function BorrowedSection({
  borrows,
  loading,
  error,
  onReturn,
  returning,
}: {
  borrows: Borrow[]
  loading: boolean
  error: string | null
  onReturn: (id: number) => void
  returning: number | null
}) {
  const active = borrows.filter((b) => b.status === 'BORROWED' || b.status === 'OVERDUE')
  if (loading) return <p className="text-sm text-gray-400">Loading…</p>
  if (error) return <p className="text-sm text-red-600">{error}</p>
  if (active.length === 0) return <p className="text-sm text-gray-500">No borrowed books.</p>
  return (
    <ul className="divide-y divide-gray-100">
      {active.map((b) => (
        <li key={b.id} className="flex items-center justify-between gap-4 py-3">
          <div className="min-w-0">
            <p className="truncate text-sm font-medium text-gray-900">Book #{b.bookId}</p>
            <p className="text-xs text-gray-500">
              Due {fmtDate(b.deadline)}
              {b.status === 'OVERDUE' && (
                <span className="ml-2 font-semibold text-red-600">Overdue</span>
              )}
            </p>
          </div>
          <button
            type="button"
            disabled={returning === b.id}
            onClick={() => onReturn(b.id)}
            className="shrink-0 rounded-lg border border-gray-200 px-3 py-1.5 text-xs font-semibold text-gray-700 transition hover:border-emerald-300 hover:bg-emerald-50 hover:text-emerald-800 disabled:opacity-40"
          >
            {returning === b.id ? 'Returning…' : 'Return'}
          </button>
        </li>
      ))}
    </ul>
  )
}

// ── Borrow History section ────────────────────────────────────────────────────

function HistorySection({
  borrows,
  loading,
  error,
}: {
  borrows: Borrow[]
  loading: boolean
  error: string | null
}) {
  const past = borrows.filter((b) => b.status === 'RETURNED')
  if (loading) return <p className="text-sm text-gray-400">Loading…</p>
  if (error) return <p className="text-sm text-red-600">{error}</p>
  if (past.length === 0) return <p className="text-sm text-gray-500">No borrow history.</p>
  return (
    <ul className="divide-y divide-gray-100">
      {past.map((b) => (
        <li key={b.id} className="flex items-center justify-between gap-4 py-3">
          <div>
            <p className="text-sm font-medium text-gray-900">Book #{b.bookId}</p>
            <p className="text-xs text-gray-500">Returned {b.returnedAt ? fmtDate(b.returnedAt) : '—'}</p>
          </div>
          <span className="rounded-full border border-emerald-200 bg-emerald-50 px-2.5 py-0.5 text-xs font-medium text-emerald-800">
            Returned
          </span>
        </li>
      ))}
    </ul>
  )
}

// ── Fines section ─────────────────────────────────────────────────────────────

function FinesSection({
  fines,
  loading,
  error,
  onAcknowledge,
  onPay,
  acting,
}: {
  fines: Fine[]
  loading: boolean
  error: string | null
  onAcknowledge: (id: number) => void
  onPay: (id: number) => void
  acting: number | null
}) {
  if (loading) return <p className="text-sm text-gray-400">Loading…</p>
  if (error) return <p className="text-sm text-red-600">{error}</p>
  if (fines.length === 0) return <p className="text-sm text-gray-500">No fines.</p>
  return (
    <ul className="divide-y divide-gray-100">
      {fines.map((f) => (
        <li key={f.id} className="flex items-center justify-between gap-4 py-3">
          <div>
            <p className="text-sm font-medium text-gray-900">€{f.amount.toFixed(2)}</p>
            <p className="text-xs text-gray-500">Issued {fmtDate(f.issuedAt)}</p>
          </div>
          <div className="flex gap-2">
            {!f.acknowledged && (
              <button
                type="button"
                disabled={acting === f.id}
                onClick={() => onAcknowledge(f.id)}
                className="rounded-lg border border-gray-200 px-3 py-1.5 text-xs font-semibold text-gray-700 transition hover:border-yellow-300 hover:bg-yellow-50 hover:text-yellow-800 disabled:opacity-40"
              >
                Acknowledge
              </button>
            )}
            {!f.paid && (
              <button
                type="button"
                disabled={acting === f.id}
                onClick={() => onPay(f.id)}
                className="rounded-lg bg-emerald-800 px-3 py-1.5 text-xs font-semibold text-white transition hover:bg-emerald-900 disabled:opacity-40"
              >
                {acting === f.id ? 'Processing…' : 'Pay'}
              </button>
            )}
            {f.paid && (
              <span className="rounded-full border border-emerald-200 bg-emerald-50 px-2.5 py-0.5 text-xs font-medium text-emerald-800">
                Paid
              </span>
            )}
          </div>
        </li>
      ))}
    </ul>
  )
}

// ── Notifications section ─────────────────────────────────────────────────────

function NotificationsSection({
  notifications,
  loading,
  error,
  onMarkRead,
}: {
  notifications: Notification[]
  loading: boolean
  error: string | null
  onMarkRead: (id: number) => void
}) {
  if (loading) return <p className="text-sm text-gray-400">Loading…</p>
  if (error) return <p className="text-sm text-red-600">{error}</p>
  if (notifications.length === 0) return <p className="text-sm text-gray-500">No notifications.</p>
  return (
    <ul className="divide-y divide-gray-100">
      {notifications.map((n) => (
        <li key={n.id} className={`flex items-start justify-between gap-4 py-3 ${n.read ? 'opacity-60' : ''}`}>
          <div className="min-w-0 flex-1">
            <p className="text-sm text-gray-800">{n.message}</p>
            <p className="mt-0.5 text-xs text-gray-400">{fmtDate(n.createdAt)}</p>
          </div>
          {!n.read && (
            <button
              type="button"
              onClick={() => onMarkRead(n.id)}
              className="shrink-0 rounded-lg border border-gray-200 px-3 py-1.5 text-xs font-semibold text-gray-600 transition hover:bg-gray-50 disabled:opacity-40"
            >
              Mark read
            </button>
          )}
        </li>
      ))}
    </ul>
  )
}

// ── Page ──────────────────────────────────────────────────────────────────────

export function StudentDashboardPage() {
  const dispatch = useAppDispatch()
  const navigate = useNavigate()
  const profile = useAppSelector((s) => s.user.profile)
  const [section, setSection] = useState<DashboardSection>('borrowed')

  const [borrows, setBorrows] = useState<Borrow[]>([])
  const [borrowsLoading, setBorrowsLoading] = useState(true)
  const [borrowsError, setBorrowsError] = useState<string | null>(null)
  const [returning, setReturning] = useState<number | null>(null)

  const [fines, setFines] = useState<Fine[]>([])
  const [finesLoading, setFinesLoading] = useState(true)
  const [finesError, setFinesError] = useState<string | null>(null)
  const [actingFine, setActingFine] = useState<number | null>(null)

  const [notifications, setNotifications] = useState<Notification[]>([])
  const [notifLoading, setNotifLoading] = useState(true)
  const [notifError, setNotifError] = useState<string | null>(null)

  const displayName = useMemo(() => displayNameFromProfile(profile), [profile])

  useEffect(() => {
    if (!profile?.id) return
    const uid = profile.id
    getBorrowsForUser(uid)
      .then(setBorrows)
      .catch((e: unknown) => setBorrowsError(e instanceof Error ? e.message : 'Failed to load'))
      .finally(() => setBorrowsLoading(false))
    getFinesForUser(uid)
      .then(setFines)
      .catch((e: unknown) => setFinesError(e instanceof Error ? e.message : 'Failed to load'))
      .finally(() => setFinesLoading(false))
    getNotificationsForUser(uid)
      .then(setNotifications)
      .catch((e: unknown) => setNotifError(e instanceof Error ? e.message : 'Failed to load'))
      .finally(() => setNotifLoading(false))
  }, [profile?.id])

  async function handleReturn(borrowId: number) {
    setReturning(borrowId)
    try {
      const updated = await returnBorrow(borrowId)
      setBorrows((prev) => prev.map((b) => (b.id === updated.id ? updated : b)))
    } catch (e) {
      alert(e instanceof Error ? e.message : 'Failed to return book')
    } finally {
      setReturning(null)
    }
  }

  async function handleAcknowledge(fineId: number) {
    setActingFine(fineId)
    try {
      await acknowledgeFine(fineId)
      setFines((prev) => prev.map((f) => (f.id === fineId ? { ...f, acknowledged: true } : f)))
    } catch (e) {
      alert(e instanceof Error ? e.message : 'Failed to acknowledge fine')
    } finally {
      setActingFine(null)
    }
  }

  async function handlePay(fineId: number) {
    setActingFine(fineId)
    try {
      await payFine(fineId)
      setFines((prev) => prev.map((f) => (f.id === fineId ? { ...f, paid: true } : f)))
    } catch (e) {
      alert(e instanceof Error ? e.message : 'Failed to pay fine')
    } finally {
      setActingFine(null)
    }
  }

  async function handleMarkRead(notifId: number) {
    try {
      await markNotificationRead(notifId)
      setNotifications((prev) => prev.map((n) => (n.id === notifId ? { ...n, read: true } : n)))
    } catch (e) {
      alert(e instanceof Error ? e.message : 'Failed to mark notification as read')
    }
  }

  const onLogout = () => {
    dispatch(logout())
    navigate(APP_ROUTES.LOGIN, { replace: true })
  }

  const unreadCount = notifications.filter((n) => !n.read).length
  const unpaidFines = fines.filter((f) => !f.paid).length

  function sectionLabel(id: DashboardSection) {
    if (id === 'notifications' && unreadCount > 0) return `Notifications (${unreadCount})`
    if (id === 'fines' && unpaidFines > 0) return `Fines (${unpaidFines})`
    return SECTIONS.find((s) => s.id === id)?.label ?? id
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
                  {sectionLabel(item.id)}
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
            <h2 className="text-lg font-semibold text-gray-900">{sectionLabel(section)}</h2>
            <div className="mt-5">
              {section === 'borrowed' && (
                <BorrowedSection
                  borrows={borrows}
                  loading={borrowsLoading}
                  error={borrowsError}
                  onReturn={handleReturn}
                  returning={returning}
                />
              )}
              {section === 'history' && (
                <HistorySection
                  borrows={borrows}
                  loading={borrowsLoading}
                  error={borrowsError}
                />
              )}
              {section === 'fines' && (
                <FinesSection
                  fines={fines}
                  loading={finesLoading}
                  error={finesError}
                  onAcknowledge={handleAcknowledge}
                  onPay={handlePay}
                  acting={actingFine}
                />
              )}
              {section === 'notifications' && (
                <NotificationsSection
                  notifications={notifications}
                  loading={notifLoading}
                  error={notifError}
                  onMarkRead={handleMarkRead}
                />
              )}
            </div>
          </div>
        </main>
      </div>
    </div>
  )
}
