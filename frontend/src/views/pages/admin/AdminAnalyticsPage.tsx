import { useEffect, useState } from 'react'
import { ADMIN_DASHBOARD_NAV } from '../../../constants/adminNav'
import { DashboardShell } from '../../../layout/DashboardShell'
import { getAdminAnalytics } from '../../../http/adminService'
import type { AdminAnalytics } from '../../../types/admin'

function MetricCard({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-xl border border-gray-200/80 bg-white p-5 shadow-sm shadow-gray-200/60">
      <p className="text-sm font-medium text-gray-500">{label}</p>
      <p className="mt-2 text-2xl font-semibold tracking-tight text-gray-900">{value}</p>
    </div>
  )
}

export function AdminAnalyticsPage() {
  const [analytics, setAnalytics] = useState<AdminAnalytics | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setLoading(true)
    getAdminAnalytics()
      .then(setAnalytics)
      .catch((err: unknown) => setError(err instanceof Error ? err.message : 'Failed to load analytics'))
      .finally(() => setLoading(false))
  }, [])

  return (
    <DashboardShell roleLabel="Administrator" navItems={[...ADMIN_DASHBOARD_NAV]}>
      <div className="mx-auto max-w-4xl space-y-6">
        <div>
          <h1 className="text-2xl font-semibold text-gray-900">Analytics</h1>
          <p className="mt-1 text-sm text-gray-600">
            Library usage and lending metrics.
          </p>
        </div>

        {loading ? (
          <p className="py-8 text-center text-sm text-gray-500">Loading analytics...</p>
        ) : error ? (
          <p className="py-8 text-center text-sm text-red-600">{error}</p>
        ) : analytics ? (
          <div>
            <h2 className="mb-3 text-sm font-semibold uppercase tracking-wide text-gray-500">
              Lending overview
            </h2>
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              <MetricCard label="Total borrows (all time)" value={String(analytics.totalBorrows)} />
              <MetricCard label="Currently borrowed" value={String(analytics.activeBorrows)} />
              <MetricCard label="Overdue" value={String(analytics.overdueBorrows)} />
              <MetricCard label="Fines paid" value={String(analytics.totalFinesCollected)} />
              <MetricCard label="Unpaid fines" value={String(analytics.unpaidFines)} />
            </div>
          </div>
        ) : null}
      </div>
    </DashboardShell>
  )
}
