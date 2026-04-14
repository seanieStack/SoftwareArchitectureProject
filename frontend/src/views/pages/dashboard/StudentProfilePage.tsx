import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { APP_ROUTES } from '../../../constants/routes'
import { WEBSITE_LOGO_SRC } from '../../../constants/assets'
import { useAppDispatch, useAppSelector } from '../../../redux-features/store/hooks'
import { logout } from '../../../redux-features/slices/user/userSlice'
import {
  getBorrowsForUser,
  getFinesForUser,
  getNotificationsForUser,
  returnBorrow,
  acknowledgeFine,
} from '../../../http/supportService'
import type { Borrow, Fine, Notification } from '../../../types/support'

// ── Helpers ───────────────────────────────────────────────────────────────────

function formatDate(iso: string | null): string {
  if (!iso) return '—'
  return new Date(iso).toLocaleDateString('en-IE', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  })
}

function isOverdue(deadline: string): boolean {
  return new Date(deadline) < new Date()
}

// ── Sub-components ────────────────────────────────────────────────────────────

function SectionCard({
  title,
  children,
}: {
  title: string
  children: React.ReactNode
}) {
  return (
    <section className="rounded-2xl border border-gray-200/80 bg-white shadow-sm">
      <h2 className="border-b border-gray-100 px-5 py-4 text-sm font-semibold text-gray-900">
        {title}
      </h2>
      <div className="p-5">{children}</div>
    </section>
  )
}

function EmptyState({ message }: { message: string }) {
  return (
    <p className="rounded-lg border border-dashed border-gray-200 bg-gray-50 px-4 py-6 text-center text-sm text-gray-500">
      {message}
    </p>
  )
}

function StatusBadge({ status }: { status: Borrow['status'] }) {
  const styles: Record<Borrow['status'], string> = {
    BORROWED: 'bg-emerald-50 text-emerald-800 border-emerald-200',
    OVERDUE: 'bg-red-50 text-red-700 border-red-200',
    RETURNED: 'bg-gray-100 text-gray-600 border-gray-200',
  }
  return (
    <span className={`inline-flex rounded-full border px-2.5 py-0.5 text-xs font-medium ${styles[status]}`}>
      {status.charAt(0) + status.slice(1).toLowerCase()}
    </span>
  )
}

// ── Page ──────────────────────────────────────────────────────────────────────

export function StudentProfilePage() {
  const dispatch = useAppDispatch()
  const navigate = useNavigate()
  const profile = useAppSelector((s) => s.user.profile)

  const [borrows, setBorrows] = useState<Borrow[]>([])
  const [fines, setFines] = useState<Fine[]>([])
  const [notifications, setNotifications] = useState<Notification[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!profile) return
    Promise.all([
      getBorrowsForUser(profile.id),
      getFinesForUser(profile.id),
      getNotificationsForUser(profile.id),
    ])
      .then(([b, f, n]) => {
        setBorrows(b)
        setFines(f)
        setNotifications(n)
      })
      .catch((err: unknown) =>
        setError(err instanceof Error ? err.message : 'Failed to load profile data'),
      )
      .finally(() => setLoading(false))
  }, [profile])

  const activeBorrows = borrows.filter((b) => b.status !== 'RETURNED')
  const borrowHistory = borrows.filter((b) => b.status === 'RETURNED')
  const unpaidFines = fines.filter((f) => !f.paid)
  const unreadNotifications = notifications.filter((n) => !n.read)

  async function handleReturn(borrowId: number) {
    try {
      const updated = await returnBorrow(borrowId)
      setBorrows((prev) => prev.map((b) => (b.id === borrowId ? updated : b)))
    } catch (err) {
      alert(err instanceof Error ? err.message : 'Failed to return book')
    }
  }

  async function handleAcknowledgeFine(fineId: number) {
    try {
      await acknowledgeFine(fineId)
      setFines((prev) =>
        prev.map((f) => (f.id === fineId ? { ...f, acknowledged: true } : f)),
      )
    } catch (err) {
      alert(err instanceof Error ? err.message : 'Failed to acknowledge fine')
    }
  }

  const onLogout = () => {
    dispatch(logout())
    navigate(APP_ROUTES.LOGIN, { replace: true })
  }

  return (
    <div className="min-h-screen bg-[#f6f7f9]">
      {/* Header */}
      <header className="sticky top-0 z-20 border-b border-gray-200/90 bg-white/95 backdrop-blur">
        <div className="flex w-full items-center justify-between gap-4 px-4 py-3 sm:px-6 lg:px-8">
          <img
            src={WEBSITE_LOGO_SRC}
            alt="UL E-Library"
            className="h-9 w-auto object-contain"
            width={160}
            height={36}
          />
          <Link
            to={APP_ROUTES.STUDENT_DASHBOARD}
            className="text-sm font-semibold text-emerald-800 hover:text-emerald-900"
          >
            ← Back to dashboard
          </Link>
        </div>
      </header>

      <main className="mx-auto max-w-4xl space-y-6 px-4 py-8 sm:px-6 lg:px-8">

        {/* Profile card */}
        <div className="flex flex-col gap-4 rounded-2xl border border-emerald-200/50 bg-gradient-to-br from-emerald-50/80 via-white to-white p-6 shadow-sm sm:flex-row sm:items-center sm:justify-between">
          <div className="flex items-center gap-4">
            <div className="flex h-14 w-14 shrink-0 items-center justify-center rounded-full bg-emerald-800 text-xl font-bold text-white">
              {profile?.fullName?.charAt(0).toUpperCase() ?? '?'}
            </div>
            <div>
              <p className="text-lg font-semibold text-gray-900">{profile?.fullName ?? '—'}</p>
              <p className="text-sm text-gray-500">{profile?.email ?? '—'}</p>
              <span className="mt-1 inline-flex rounded-full border border-emerald-200 bg-emerald-50 px-2.5 py-0.5 text-xs font-medium capitalize text-emerald-900">
                {profile?.userType ?? '—'}
              </span>
            </div>
          </div>
          <div className="flex flex-wrap gap-2 text-center">
            <div className="rounded-xl border border-gray-200 bg-white px-4 py-2 shadow-sm">
              <p className="text-2xl font-bold text-gray-900">{activeBorrows.length}</p>
              <p className="text-xs text-gray-500">Active borrows</p>
            </div>
            <div className="rounded-xl border border-gray-200 bg-white px-4 py-2 shadow-sm">
              <p className={`text-2xl font-bold ${unpaidFines.length > 0 ? 'text-red-600' : 'text-gray-900'}`}>
                {unpaidFines.length}
              </p>
              <p className="text-xs text-gray-500">Unpaid fines</p>
            </div>
            <div className="rounded-xl border border-gray-200 bg-white px-4 py-2 shadow-sm">
              <p className={`text-2xl font-bold ${unreadNotifications.length > 0 ? 'text-emerald-700' : 'text-gray-900'}`}>
                {unreadNotifications.length}
              </p>
              <p className="text-xs text-gray-500">Unread</p>
            </div>
          </div>
        </div>

        {error && (
          <p className="rounded-xl border border-red-200 bg-red-50 px-4 py-4 text-sm text-red-700">
            {error}
          </p>
        )}

        {loading ? (
          <div className="space-y-4">
            {[160, 120, 100].map((h) => (
              <div key={h} className={`h-${h} animate-pulse rounded-2xl bg-gray-200`} aria-hidden />
            ))}
          </div>
        ) : (
          <>
            {/* Active borrows */}
            <SectionCard title={`Active borrows (${activeBorrows.length})`}>
              {activeBorrows.length === 0 ? (
                <EmptyState message="No active borrows." />
              ) : (
                <ul className="divide-y divide-gray-100">
                  {activeBorrows.map((b) => {
                    const overdue = b.status === 'OVERDUE' || (b.status === 'BORROWED' && isOverdue(b.deadline))
                    return (
                      <li key={b.id} className="flex items-center justify-between gap-4 py-3">
                        <div className="min-w-0">
                          <p className="text-sm font-medium text-gray-800">Book #{b.bookId}</p>
                          <p className={`text-xs ${overdue ? 'text-red-600 font-semibold' : 'text-gray-500'}`}>
                            Due {formatDate(b.deadline)}{overdue ? ' — overdue' : ''}
                          </p>
                        </div>
                        <div className="flex shrink-0 items-center gap-2">
                          <StatusBadge status={overdue && b.status !== 'RETURNED' ? 'OVERDUE' : b.status} />
                          <button
                            type="button"
                            onClick={() => handleReturn(b.id)}
                            className="rounded-lg border border-gray-200 px-3 py-1.5 text-xs font-semibold text-gray-700 transition hover:bg-gray-50"
                          >
                            Return
                          </button>
                        </div>
                      </li>
                    )
                  })}
                </ul>
              )}
            </SectionCard>

            {/* Fines */}
            <SectionCard title={`Fines (${fines.length})`}>
              {fines.length === 0 ? (
                <EmptyState message="No fines on your account." />
              ) : (
                <ul className="divide-y divide-gray-100">
                  {fines.map((f) => (
                    <li key={f.id} className="flex items-center justify-between gap-4 py-3">
                      <div className="min-w-0">
                        <p className="text-sm font-medium text-gray-800">€{Number(f.amount).toFixed(2)}</p>
                        <p className="text-xs text-gray-500">Issued {formatDate(f.issuedAt)}</p>
                      </div>
                      <div className="flex shrink-0 items-center gap-2">
                        {f.paid ? (
                          <span className="rounded-full border border-emerald-200 bg-emerald-50 px-2.5 py-0.5 text-xs font-medium text-emerald-800">
                            Paid
                          </span>
                        ) : (
                          <span className="rounded-full border border-red-200 bg-red-50 px-2.5 py-0.5 text-xs font-medium text-red-700">
                            Unpaid
                          </span>
                        )}
                        {!f.acknowledged && (
                          <button
                            type="button"
                            onClick={() => handleAcknowledgeFine(f.id)}
                            className="rounded-lg border border-gray-200 px-3 py-1.5 text-xs font-semibold text-gray-700 transition hover:bg-gray-50"
                          >
                            Acknowledge
                          </button>
                        )}
                      </div>
                    </li>
                  ))}
                </ul>
              )}
            </SectionCard>

            {/* Borrow history */}
            <SectionCard title={`Borrow history (${borrowHistory.length})`}>
              {borrowHistory.length === 0 ? (
                <EmptyState message="No returned books yet." />
              ) : (
                <ul className="divide-y divide-gray-100">
                  {borrowHistory.map((b) => (
                    <li key={b.id} className="flex items-center justify-between gap-4 py-3">
                      <div className="min-w-0">
                        <p className="text-sm font-medium text-gray-800">Book #{b.bookId}</p>
                        <p className="text-xs text-gray-500">
                          Borrowed {formatDate(b.borrowedAt)} · Returned {formatDate(b.returnedAt)}
                        </p>
                      </div>
                      <StatusBadge status={b.status} />
                    </li>
                  ))}
                </ul>
              )}
            </SectionCard>

            {/* Notifications */}
            <SectionCard title={`Notifications (${notifications.length})`}>
              {notifications.length === 0 ? (
                <EmptyState message="No notifications." />
              ) : (
                <ul className="divide-y divide-gray-100">
                  {notifications.map((n) => (
                    <li key={n.id} className={`py-3 ${!n.read ? 'opacity-100' : 'opacity-60'}`}>
                      <div className="flex items-start gap-3">
                        {!n.read && (
                          <span className="mt-1.5 h-2 w-2 shrink-0 rounded-full bg-emerald-600" aria-hidden />
                        )}
                        <div className={!n.read ? '' : 'pl-5'}>
                          <p className="text-sm text-gray-800">{n.message}</p>
                          <p className="mt-0.5 text-xs text-gray-400">
                            {n.type} · {formatDate(n.createdAt)}
                          </p>
                        </div>
                      </div>
                    </li>
                  ))}
                </ul>
              )}
            </SectionCard>

            {/* Danger zone */}
            <div className="rounded-2xl border border-red-200/60 bg-white p-5">
              <p className="text-sm font-semibold text-gray-700">Account</p>
              <p className="mt-1 text-xs text-gray-500">Signing out clears your local session.</p>
              <button
                type="button"
                onClick={onLogout}
                className="mt-3 rounded-lg border border-red-200 bg-red-50 px-4 py-2 text-sm font-semibold text-red-700 transition hover:bg-red-100"
              >
                Sign out
              </button>
            </div>
          </>
        )}
      </main>
    </div>
  )
}
