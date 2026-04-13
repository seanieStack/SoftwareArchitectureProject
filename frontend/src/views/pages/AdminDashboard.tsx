import { ADMIN_DASHBOARD_NAV } from '../../constants/adminNav'
import { DashboardShell } from '../../layout/DashboardShell'

function StatCard({
  label,
  value,
  hint,
}: {
  label: string
  value: string
  hint?: string
}) {
  return (
    <div className="rounded-xl border border-gray-200/80 bg-white p-5 shadow-sm shadow-gray-200/60">
      <p className="text-sm font-medium text-gray-500">{label}</p>
      <p className="mt-2 text-2xl font-semibold tracking-tight text-gray-900">{value}</p>
      {hint ? <p className="mt-1 text-xs text-gray-500">{hint}</p> : null}
    </div>
  )
}

export function AdminDashboard() {
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
            what&apos;s on loan. The numbers below will show live totals once your team connects
            the admin tools to the library system.
          </p>
        </div>

        <div>
          <h2 className="mb-3 text-sm font-semibold uppercase tracking-wide text-gray-500">
            At a glance
          </h2>
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            <StatCard label="Active loans" value="—" hint="Live count coming soon" />
            <StatCard label="Titles in catalog" value="—" hint="Live count coming soon" />
            <StatCard label="Registered users" value="—" hint="Live count coming soon" />
          </div>
        </div>
      </div>
    </DashboardShell>
  )
}
