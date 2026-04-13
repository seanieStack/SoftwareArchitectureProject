import { ADMIN_DASHBOARD_NAV } from '../../../constants/adminNav'
import { DashboardShell } from '../../../layout/DashboardShell'

export function AdminAnalyticsPage() {
  return (
    <DashboardShell roleLabel="Administrator" navItems={[...ADMIN_DASHBOARD_NAV]}>
      <div className="mx-auto max-w-4xl space-y-4">
        <div>
          <h1 className="text-2xl font-semibold text-gray-900">Analytics</h1>
          <p className="mt-1 text-sm text-gray-600">
            Library usage and lending metrics will appear here when reporting is available.
          </p>
        </div>
        <div className="rounded-xl border border-gray-200/80 bg-white p-8 shadow-md shadow-gray-200/80">
          <p className="text-sm text-gray-500">No analytics wired yet.</p>
        </div>
      </div>
    </DashboardShell>
  )
}
