import { useEffect, useState } from 'react'
import { ADMIN_DASHBOARD_NAV } from '../../../constants/adminNav'
import { DashboardShell } from '../../../layout/DashboardShell'
import { getAdminUsers } from '../../../http/adminService'
import type { AdminUser } from '../../../types/admin'

const ROLE_BADGE: Record<string, string> = {
  student: 'border-blue-200 bg-blue-50 text-blue-800',
  staff:   'border-amber-200 bg-amber-50 text-amber-800',
  admin:   'border-emerald-200 bg-emerald-50 text-emerald-800',
}

export function AdminUserManagementPage() {
  const [users, setUsers] = useState<AdminUser[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [search, setSearch] = useState('')

  useEffect(() => {
    getAdminUsers()
      .then(setUsers)
      .catch((e: unknown) => setError(e instanceof Error ? e.message : 'Failed to load users'))
      .finally(() => setLoading(false))
  }, [])

  const filtered = users.filter((u) => {
    const q = search.trim().toLowerCase()
    if (!q) return true
    return (
      u.fullName.toLowerCase().includes(q) ||
      u.email.toLowerCase().includes(q) ||
      u.userType.toLowerCase().includes(q)
    )
  })

  return (
    <DashboardShell roleLabel="Administrator" navItems={[...ADMIN_DASHBOARD_NAV]}>
      <div className="mx-auto max-w-5xl space-y-6">
        <div>
          <h1 className="text-2xl font-semibold text-gray-900">User management</h1>
          <p className="mt-1 text-sm text-gray-600">All registered members and their roles.</p>
        </div>

        <div className="rounded-xl border border-gray-200/80 bg-white shadow-md shadow-gray-200/80">
          <div className="space-y-4 p-4 md:p-6">
            <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
              <input
                type="search"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Search by name, email, or role…"
                className="w-full rounded-lg border border-gray-200 bg-gray-50 py-2 px-3 text-sm text-gray-900 placeholder:text-gray-400 focus:border-emerald-700 focus:bg-white focus:outline-none focus:ring-2 focus:ring-emerald-700/20 sm:max-w-xs"
                autoComplete="off"
              />
              {!loading && !error && (
                <p className="shrink-0 text-sm text-gray-500">
                  <span className="font-medium text-gray-700">{filtered.length}</span> of {users.length} users
                </p>
              )}
            </div>

            {loading && <p className="py-8 text-center text-sm text-gray-400">Loading users…</p>}
            {error && <p className="py-8 text-center text-sm text-red-600">{error}</p>}

            {!loading && !error && (
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b border-gray-100 text-left text-xs font-semibold uppercase tracking-wide text-gray-500">
                      <th className="pb-2 pr-4">Name</th>
                      <th className="pb-2 pr-4">Email</th>
                      <th className="pb-2">Role</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-50">
                    {filtered.length === 0 ? (
                      <tr>
                        <td colSpan={3} className="py-8 text-center text-gray-400">
                          No users match your search.
                        </td>
                      </tr>
                    ) : (
                      filtered.map((u) => (
                        <tr key={u.id} className="hover:bg-gray-50/60">
                          <td className="py-3 pr-4 font-medium text-gray-900">{u.fullName}</td>
                          <td className="py-3 pr-4 text-gray-600">{u.email}</td>
                          <td className="py-3">
                            <span
                              className={`inline-flex rounded-full border px-2.5 py-0.5 text-xs font-medium capitalize ${ROLE_BADGE[u.userType] ?? 'border-gray-200 bg-gray-50 text-gray-700'}`}
                            >
                              {u.userType}
                            </span>
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      </div>
    </DashboardShell>
  )
}
