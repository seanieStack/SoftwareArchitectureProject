import { useEffect, useState } from 'react'
import { ADMIN_DASHBOARD_NAV } from '../../../constants/adminNav'
import { DashboardShell } from '../../../layout/DashboardShell'
import { getAdminAnalytics } from '../../../http/adminService'
import type { AdminAnalytics } from '../../../types/admin'

function MetricCard({
  label,
  value,
  accent,
}: {
  label: string
  value: number | string
  accent?: 'green' | 'red' | 'amber' | 'blue'
}) {
  const colours: Record<string, string> = {
    green: 'border-emerald-200 bg-emerald-50 text-emerald-900',
    red:   'border-red-200 bg-red-50 text-red-900',
    amber: 'border-amber-200 bg-amber-50 text-amber-900',
    blue:  'border-blue-200 bg-blue-50 text-blue-900',
  }
  return (
    <div className={`rounded-xl border p-5 ${accent ? colours[accent] : 'border-gray-200 bg-white text-gray-900'}`}>
      <p className="text-sm font-medium opacity-70">{label}</p>
      <p className="mt-2 text-3xl font-bold tracking-tight">
        {value === null || value === undefined ? '—' : String(value)}
      </p>
    </div>
  )
}

export function AdminAnalyticsPage() {
  const [data, setData] = useState<AdminAnalytics | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    getAdminAnalytics()
      .then(setData)
      .catch((e: unknown) => setError(e instanceof Error ? e.message : 'Failed to load analytics'))
      .finally(() => setLoading(false))
  }, [])

  const returnedBorrows = data
    ? Math.max(data.totalBorrows - data.activeBorrows - data.overdueBorrows, 0)
    : 0
  const loanTotal = data
    ? data.activeBorrows + data.overdueBorrows + returnedBorrows
    : 0

  return (
    <DashboardShell roleLabel="Administrator" navItems={[...ADMIN_DASHBOARD_NAV]}>
      <div className="mx-auto max-w-4xl space-y-6">
        <div>
          <h1 className="text-2xl font-semibold text-gray-900">Analytics</h1>
          <p className="mt-1 text-sm text-gray-600">Library lending metrics at a glance.</p>
        </div>

        {loading && <p className="text-sm text-gray-400">Loading analytics…</p>}
        {error && <p className="text-sm text-red-600">{error}</p>}

        {!loading && !error && data && (
          <>
            <div>
              <h2 className="mb-3 text-sm font-semibold uppercase tracking-wide text-gray-500">
                Loan status
              </h2>
              <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
                <MetricCard label="Active loans" value={data.activeBorrows} accent="green" />
                <MetricCard label="Overdue loans" value={data.overdueBorrows} accent="red" />
                <MetricCard label="Returned (all time)" value={returnedBorrows} accent="blue" />
                <MetricCard label="Outstanding fines" value={data.unpaidFines} accent="amber" />
              </div>
            </div>

            <div className="rounded-xl border border-gray-200/80 bg-white p-6 shadow-sm">
              <h2 className="mb-4 text-sm font-semibold text-gray-700">Loan breakdown</h2>
              {(() => {
                if (loanTotal === 0) return <p className="text-sm text-gray-400">No loan data yet.</p>
                const bars: { label: string; count: number; colour: string }[] = [
                  { label: 'Active', count: data.activeBorrows, colour: 'bg-emerald-500' },
                  { label: 'Overdue', count: data.overdueBorrows, colour: 'bg-red-500' },
                  { label: 'Returned', count: returnedBorrows, colour: 'bg-blue-400' },
                ]
                return (
                  <div className="space-y-3">
                    {bars.map((b) => (
                      <div key={b.label}>
                        <div className="mb-1 flex justify-between text-xs text-gray-600">
                          <span>{b.label}</span>
                          <span className="font-medium">{b.count}</span>
                        </div>
                        <div className="h-2 w-full overflow-hidden rounded-full bg-gray-100">
                          <div
                            className={`h-2 rounded-full ${b.colour}`}
                            style={{ width: `${Math.round((b.count / loanTotal) * 100)}%` }}
                          />
                        </div>
                      </div>
                    ))}
                  </div>
                )
              })()}
            </div>
          </>
        )}
      </div>
    </DashboardShell>
  )
}
