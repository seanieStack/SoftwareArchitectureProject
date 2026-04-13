import { useMemo, useState } from 'react'
import { ADMIN_DASHBOARD_NAV } from '../../../constants/adminNav'
import type { AdminBookRow } from '../../../types/catalog'
import { DashboardShell } from '../../../layout/DashboardShell'
import { BookManagementTable } from '../../components/tables/BookManagementTable'
import { SearchIcon } from '../../components/utils/adminIcons'

function matchesSearch(book: AdminBookRow, q: string) {
  if (!q.trim()) return true
  const s = q.trim().toLowerCase()
  return (
    book.title.toLowerCase().includes(s) ||
    book.author.toLowerCase().includes(s) ||
    book.isbn.replace(/\s/g, '').includes(s.replace(/\s/g, ''))
  )
}

export function BookManagementPage() {
  const [search, setSearch] = useState('')
  const [categoryFilter, setCategoryFilter] = useState('')
  const [groupByCategory, setGroupByCategory] = useState(false)

  /** Replace with Redux/API-backed list when the admin catalogue endpoint is wired. */
  const books: AdminBookRow[] = []

  const hasFilters = Boolean(search.trim() || categoryFilter)

  const categories = useMemo(() => {
    const set = new Set(books.map((b) => b.category))
    return Array.from(set).sort((a, b) => a.localeCompare(b))
  }, [books])

  const filteredBooks = useMemo(() => {
    return books.filter((book) => {
      if (!matchesSearch(book, search)) return false
      if (categoryFilter && book.category !== categoryFilter) return false
      return true
    })
  }, [books, search, categoryFilter])

  const groupedBooks = useMemo(() => {
    const map = new Map<string, AdminBookRow[]>()
    for (const book of filteredBooks) {
      const list = map.get(book.category) ?? []
      list.push(book)
      map.set(book.category, list)
    }
    return Array.from(map.entries()).sort((a, b) => a[0].localeCompare(b[0]))
  }, [filteredBooks])

  return (
    <DashboardShell roleLabel="Administrator" navItems={[...ADMIN_DASHBOARD_NAV]}>
      <div className="mx-auto max-w-6xl space-y-6">
        <div>
          <h1 className="text-2xl font-semibold text-gray-900">Book management</h1>
          <p className="mt-1 text-sm text-gray-600">
            Search, filter by category, or view titles grouped by category once the catalogue is
            loaded from the API.
          </p>
        </div>

        <div className="rounded-xl border border-gray-200/80 bg-white shadow-md shadow-gray-200/80">
          <div className="space-y-4 p-4 md:p-6">
            <div className="flex flex-col gap-3 lg:flex-row lg:items-end lg:justify-between lg:gap-4">
              <div className="min-w-0 flex-1 space-y-3">
                <label className="block text-xs font-medium uppercase tracking-wide text-gray-500">
                  Search
                </label>
                <div className="relative">
                  <SearchIcon className="pointer-events-none absolute left-3 top-1/2 size-5 -translate-y-1/2 text-gray-400" />
                  <input
                    type="search"
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                    placeholder="Search books by title, author, or ISBN…"
                    className="w-full rounded-lg border border-gray-200 bg-gray-50 py-2.5 pl-10 pr-3 text-sm text-gray-900 placeholder:text-gray-400 focus:border-emerald-700 focus:bg-white focus:outline-none focus:ring-2 focus:ring-emerald-700/20"
                    autoComplete="off"
                  />
                </div>
              </div>
              <div className="flex flex-wrap items-end gap-3">
                <div>
                  <label
                    htmlFor="book-category-filter"
                    className="mb-1 block text-xs font-medium uppercase tracking-wide text-gray-500"
                  >
                    Category
                  </label>
                  <select
                    id="book-category-filter"
                    value={categoryFilter}
                    onChange={(e) => setCategoryFilter(e.target.value)}
                    className="min-w-[10rem] rounded-lg border border-gray-200 bg-white py-2.5 pl-3 pr-8 text-sm text-gray-900 focus:border-emerald-700 focus:outline-none focus:ring-2 focus:ring-emerald-700/20"
                  >
                    <option value="">All categories</option>
                    {categories.map((c) => (
                      <option key={c} value={c}>
                        {c}
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label
                    htmlFor="book-group-toggle"
                    className="mb-1 block text-xs font-medium uppercase tracking-wide text-gray-500"
                  >
                    Layout
                  </label>
                  <select
                    id="book-group-toggle"
                    value={groupByCategory ? 'grouped' : 'list'}
                    onChange={(e) => setGroupByCategory(e.target.value === 'grouped')}
                    className="min-w-[10rem] rounded-lg border border-gray-200 bg-white py-2.5 pl-3 pr-8 text-sm text-gray-900 focus:border-emerald-700 focus:outline-none focus:ring-2 focus:ring-emerald-700/20"
                  >
                    <option value="list">Flat list</option>
                    <option value="grouped">Group by category</option>
                  </select>
                </div>
                <button
                  type="button"
                  className="rounded-lg bg-emerald-800 px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition hover:bg-emerald-900"
                >
                  + Add book
                </button>
              </div>
            </div>

            <p className="text-sm text-gray-500">
              Showing{' '}
              <span className="font-medium text-gray-700">{filteredBooks.length}</span> of{' '}
              {books.length} titles
            </p>

            {!groupByCategory ? (
              <BookManagementTable books={filteredBooks} hasFilters={hasFilters} />
            ) : groupedBooks.length === 0 ? (
              <BookManagementTable books={[]} hasFilters={hasFilters} />
            ) : (
              <div className="space-y-8">
                {groupedBooks.map(([category, sectionBooks]) => (
                  <section key={category} aria-labelledby={`cat-${category}`}>
                    <h2
                      id={`cat-${category}`}
                      className="mb-3 flex items-center gap-2 text-sm font-semibold text-gray-900"
                    >
                      <span className="inline-flex rounded-full border border-emerald-200 bg-emerald-50 px-2.5 py-0.5 text-xs font-medium text-emerald-900">
                        {category}
                      </span>
                      <span className="text-gray-400">·</span>
                      <span className="font-normal text-gray-500">{sectionBooks.length} title(s)</span>
                    </h2>
                    <BookManagementTable books={sectionBooks} hasFilters={hasFilters} />
                  </section>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </DashboardShell>
  )
}
