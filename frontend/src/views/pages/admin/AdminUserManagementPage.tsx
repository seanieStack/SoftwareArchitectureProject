import { useEffect, useMemo, useState } from 'react'
import { ADMIN_DASHBOARD_NAV } from '../../../constants/adminNav'
import { DashboardShell } from '../../../layout/DashboardShell'
import { getAdminUsers, updateUserRole, deleteUser } from '../../../http/adminService'
import type { AdminUser } from '../../../types/admin'
import { SearchIcon, TrashIcon } from '../../components/utils/adminIcons'

const ROLES = ['STUDENT', 'STAFF', 'ADMIN'] as const

export function AdminUserManagementPage() {
  const [users, setUsers] = useState<AdminUser[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [search, setSearch] = useState('')
  const [roleFilter, setRoleFilter] = useState('')

  useEffect(() => {
    setLoading(true)
    getAdminUsers()
      .then(setUsers)
      .catch((err: unknown) => setError(err instanceof Error ? err.message : 'Failed to load users'))
      .finally(() => setLoading(false))
  }, [])

  const filteredUsers = useMemo(() => {
    return users.filter((u) => {
      if (roleFilter && u.role !== roleFilter) return false
      if (search.trim()) {
        const s = search.trim().toLowerCase()
        return (
          u.fullName.toLowerCase().includes(s) ||
          u.email.toLowerCase().includes(s)
        )
      }
      return true
    })
  }, [users, search, roleFilter])

  async function handleRoleChange(user: AdminUser, newRole: typeof ROLES[number]) {
    try {
      const updated = await updateUserRole(user.id, newRole)
      setUsers((prev) => prev.map((u) => (u.id === updated.id ? updated : u)))
    } catch (err) {
      alert(err instanceof Error ? err.message : 'Failed to update role')
    }
  }

  async function handleDelete(user: AdminUser) {
    if (!window.confirm(`Delete user "${user.fullName}" (${user.email})?`)) return
    try {
      await deleteUser(user.id)
      setUsers((prev) => prev.filter((u) => u.id !== user.id))
    } catch (err) {
      alert(err instanceof Error ? err.message : 'Failed to delete user')
    }
  }

  return (
    <DashboardShell roleLabel="Administrator" navItems={[...ADMIN_DASHBOARD_NAV]}>
      <div className="mx-auto max-w-6xl space-y-6">
        <div>
          <h1 className="text-2xl font-semibold text-gray-900">User management</h1>
          <p className="mt-1 text-sm text-gray-600">
            View member accounts, change roles, and remove users.
          </p>
        </div>

        <div className="rounded-xl border border-gray-200/80 bg-white shadow-md shadow-gray-200/80">
          <div className="space-y-4 p-4 md:p-6">
            <div className="flex flex-col gap-3 lg:flex-row lg:items-end lg:gap-4">
              <div className="min-w-0 flex-1 space-y-1">
                <label className="block text-xs font-medium uppercase tracking-wide text-gray-500">
                  Search
                </label>
                <div className="relative">
                  <SearchIcon className="pointer-events-none absolute left-3 top-1/2 size-5 -translate-y-1/2 text-gray-400" />
                  <input
                    type="search"
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                    placeholder="Search by name or email..."
                    className="w-full rounded-lg border border-gray-200 bg-gray-50 py-2.5 pl-10 pr-3 text-sm text-gray-900 placeholder:text-gray-400 focus:border-emerald-700 focus:bg-white focus:outline-none focus:ring-2 focus:ring-emerald-700/20"
                    autoComplete="off"
                  />
                </div>
              </div>
              <div>
                <label
                  htmlFor="user-role-filter"
                  className="mb-1 block text-xs font-medium uppercase tracking-wide text-gray-500"
                >
                  Role
                </label>
                <select
                  id="user-role-filter"
                  value={roleFilter}
                  onChange={(e) => setRoleFilter(e.target.value)}
                  className="min-w-[10rem] rounded-lg border border-gray-200 bg-white py-2.5 pl-3 pr-8 text-sm text-gray-900 focus:border-emerald-700 focus:outline-none focus:ring-2 focus:ring-emerald-700/20"
                >
                  <option value="">All roles</option>
                  {ROLES.map((r) => (
                    <option key={r} value={r}>{r}</option>
                  ))}
                </select>
              </div>
            </div>

            {loading ? (
              <p className="py-8 text-center text-sm text-gray-500">Loading users...</p>
            ) : error ? (
              <p className="py-8 text-center text-sm text-red-600">{error}</p>
            ) : (
              <>
                <p className="text-sm text-gray-500">
                  Showing{' '}
                  <span className="font-medium text-gray-700">{filteredUsers.length}</span> of{' '}
                  {users.length} users
                </p>
                <div className="max-h-[32rem] overflow-auto">
                  <table className="w-full text-left text-sm">
                    <thead className="sticky top-0 bg-gray-50 text-xs font-semibold uppercase tracking-wide text-gray-500">
                      <tr>
                        <th className="px-4 py-3">Name</th>
                        <th className="px-4 py-3">Email</th>
                        <th className="px-4 py-3">Role</th>
                        <th className="px-4 py-3">Joined</th>
                        <th className="px-4 py-3 text-right">Actions</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-100">
                      {filteredUsers.length === 0 ? (
                        <tr>
                          <td colSpan={5} className="px-4 py-8 text-center text-gray-500">
                            No users found.
                          </td>
                        </tr>
                      ) : (
                        filteredUsers.map((user) => (
                          <tr key={user.id} className="hover:bg-gray-50/60">
                            <td className="whitespace-nowrap px-4 py-3 font-medium text-gray-900">
                              {user.fullName}
                            </td>
                            <td className="whitespace-nowrap px-4 py-3 text-gray-600">
                              {user.email}
                            </td>
                            <td className="whitespace-nowrap px-4 py-3">
                              <select
                                value={user.role}
                                onChange={(e) =>
                                  handleRoleChange(user, e.target.value as typeof ROLES[number])
                                }
                                className="rounded-md border border-gray-200 bg-white px-2 py-1 text-xs font-medium text-gray-700 focus:border-emerald-700 focus:outline-none focus:ring-1 focus:ring-emerald-700/20"
                              >
                                {ROLES.map((r) => (
                                  <option key={r} value={r}>{r}</option>
                                ))}
                              </select>
                            </td>
                            <td className="whitespace-nowrap px-4 py-3 text-gray-500">
                              {new Date(user.createdAt).toLocaleDateString()}
                            </td>
                            <td className="whitespace-nowrap px-4 py-3 text-right">
                              <button
                                type="button"
                                onClick={() => handleDelete(user)}
                                className="rounded-md p-1.5 text-gray-400 transition hover:bg-red-50 hover:text-red-600"
                                title="Delete user"
                              >
                                <TrashIcon className="size-4" />
                              </button>
                            </td>
                          </tr>
                        ))
                      )}
                    </tbody>
                  </table>
                </div>
              </>
            )}
          </div>
        </div>
      </div>
    </DashboardShell>
  )
}
