import { useEffect, useState } from 'react'
import { ADMIN_DASHBOARD_NAV } from '../../constants/adminNav'
import { DashboardShell } from '../../layout/DashboardShell'
import { getAdminCounts, getAdminAnalytics } from '../../http/adminService'
import type { AdminCounts, AdminAnalytics } from '../../types/admin'

function StatCard({
  label,
  value,
  hint,
}: {
  label: string
  value: string | number
  hint?: string
}) {
  return (
    <div className="rounded-xl border border-gray-200/80 bg-white p-5 shadow-sm shadow-gray-200/60">
      <p className="text-sm font-medium text-gray-500">{label}</p>
      <p className="mt-2 text-2xl font-semibold tracking-tight text-gray-900">
        {value === null || value === undefined ? '—' : String(value)}
      </p>
      {hint ? <p className="mt-1 text-xs text-gray-500">{hint}</p> : null}
    </div>
  )
}

export function AdminDashboard() {
  const [counts, setCounts] = useState<AdminCounts | null>(null)
  const [analytics, setAnalytics] = useState<AdminAnalytics | null>(null)

  useEffect(() => {
    getAdminCounts().then(setCounts).catch(() => undefined)
    getAdminAnalytics().then(setAnalytics).catch(() => undefined)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    Promise.all([getAdminCounts(), getAdminAnalytics()])
      .then(([c, a]) => {
        setCounts(c)
        setAnalytics(a)
      })
      .catch((err: unknown) => setError(err instanceof Error ? err.message : 'Failed to load stats'))
  }, [])

  return (
    <DashboardShell
      roleLabel="Administrator"
      navItems={[...ADMIN_DASHBOARD_NAV]}
    >
      <div className="mx-auto max-w-4xl space-y-8">
        <div>
          <h1 className="text-2xl font-semibold text-gray-900">Your admin home</h1>
          <p className="mt-1 text-sm text-gray-600">
            See how the library is doing today—loans, books, and members in one place.
          </p>
        </div>

        <div className="rounded-xl border border-gray-200/80 bg-white p-6 shadow-md shadow-gray-200/80 md:p-8">
          <h2 className="text-lg font-semibold text-gray-900">Good to see you</h2>
          <p className="mt-2 text-sm leading-relaxed text-gray-600">
            From here you&apos;ll be able to help members, keep the catalog up to date, and track
            what&apos;s on loan.
          </p>
        </div>

        <div>
          <h2 className="mb-3 text-sm font-semibold uppercase tracking-wide text-gray-500">
            Catalog &amp; users
          </h2>
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            <StatCard label="Total users" value={counts?.totalUsers ?? '—'} hint={counts ? `${counts.totalStudents} students · ${counts.totalStaff} staff` : undefined} />
            <StatCard label="Titles in catalog" value={counts?.totalBooks ?? '—'} />
            <StatCard label="Admins" value={counts?.totalAdmins ?? '—'} />
          </div>
        </div>

        <div>
          <h2 className="mb-3 text-sm font-semibold uppercase tracking-wide text-gray-500">
            Loan activity
          </h2>
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <StatCard label="Active loans" value={analytics?.activeLoans ?? '—'} />
            <StatCard label="Overdue loans" value={analytics?.overdueLoans ?? '—'} />
            <StatCard label="Returned (all time)" value={analytics?.returnedLoans ?? '—'} />
            <StatCard label="Outstanding fines" value={analytics?.outstandingFines ?? '—'} />
        {error ? (
          <p className="text-sm text-red-600">{error}</p>
        ) : (
          <div>
            <h2 className="mb-3 text-sm font-semibold uppercase tracking-wide text-gray-500">
              At a glance
            </h2>
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              <StatCard
                label="Active loans"
                value={analytics ? String(analytics.activeBorrows) : '...'}
              />
              <StatCard
                label="Titles in catalog"
                value={counts ? String(counts.totalBooks) : '...'}
              />
              <StatCard
                label="Registered users"
                value={counts ? String(counts.registeredUsers) : '...'}
              />
            </div>
          </div>
        )}
      </div>
    </DashboardShell>
  )
}
