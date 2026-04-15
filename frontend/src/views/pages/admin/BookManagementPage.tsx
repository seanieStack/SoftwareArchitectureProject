import { useEffect, useMemo, useState } from 'react'
import { ADMIN_DASHBOARD_NAV } from '../../../constants/adminNav'
import type { Book, BookRequest } from '../../../types/catalog'
import { DashboardShell } from '../../../layout/DashboardShell'
import { BookManagementTable } from '../../components/tables/BookManagementTable'
import { SearchIcon } from '../../components/utils/adminIcons'
import { getBooks, createBook, updateBook, deleteBook } from '../../../http/bookService'

// ── Helpers ───────────────────────────────────────────────────────────────────

function matchesSearch(book: Book, q: string) {
  if (!q.trim()) return true
  const s = q.trim().toLowerCase()
  return (
    book.title.toLowerCase().includes(s) ||
    book.authors.some((a) => a.toLowerCase().includes(s)) ||
    book.isbn.replace(/\s/g, '').includes(s.replace(/\s/g, ''))
  )
}

function splitLines(val: string): string[] {
  return val
    .split(',')
    .map((s) => s.trim())
    .filter(Boolean)
}

// ── Book form modal ───────────────────────────────────────────────────────────

type BookFormState = {
  title: string
  isbn: string
  authors: string
  categories: string
  totalCopies: string
  availableCopies: string
}

function emptyForm(): BookFormState {
  return { title: '', isbn: '', authors: '', categories: '', totalCopies: '1', availableCopies: '1' }
}

function bookToForm(book: Book): BookFormState {
  return {
    title: book.title,
    isbn: book.isbn,
    authors: book.authors.join(', '),
    categories: book.categories.join(', '),
    totalCopies: String(book.totalCopies),
    availableCopies: String(book.availableCopies),
  }
}

function formToRequest(form: BookFormState): BookRequest {
  return {
    title: form.title.trim(),
    isbn: form.isbn.trim(),
    authors: splitLines(form.authors),
    categories: splitLines(form.categories),
    totalCopies: parseInt(form.totalCopies, 10) || 1,
    availableCopies: parseInt(form.availableCopies, 10) || 0,
  }
}

function BookFormModal({
  book,
  onClose,
  onSave,
}: {
  book: Book | null
  onClose: () => void
  onSave: (req: BookRequest, id?: number) => Promise<void>
}) {
  const [form, setForm] = useState<BookFormState>(book ? bookToForm(book) : emptyForm())
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  function field(key: keyof BookFormState) {
    return {
      value: form[key],
      onChange: (e: React.ChangeEvent<HTMLInputElement>) =>
        setForm((prev) => ({ ...prev, [key]: e.target.value })),
    }
  }

  async function handleSubmit(e: { preventDefault: () => void }) {
    e.preventDefault()
    setSaving(true)
    setError(null)
    try {
      await onSave(formToRequest(form), book?.id)
      onClose()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Save failed')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4 backdrop-blur-sm"
      onClick={(e) => { if (e.target === e.currentTarget) onClose() }}
    >
      <div className="w-full max-w-lg rounded-2xl bg-white p-6 shadow-xl">
        <h2 className="mb-5 text-lg font-semibold text-gray-900">
          {book ? 'Edit book' : 'Add book'}
        </h2>

        <form onSubmit={handleSubmit} className="space-y-4">
          {[
            { label: 'Title', key: 'title' as const, required: true },
            { label: 'ISBN', key: 'isbn' as const, required: true },
            { label: 'Authors (comma-separated)', key: 'authors' as const, required: true },
            { label: 'Categories (comma-separated)', key: 'categories' as const, required: false },
          ].map(({ label, key, required }) => (
            <div key={key}>
              <label className="mb-1 block text-xs font-medium text-gray-700">{label}</label>
              <input
                type="text"
                required={required}
                {...field(key)}
                className="w-full rounded-lg border border-gray-200 bg-gray-50 px-3 py-2 text-sm text-gray-900 focus:border-emerald-700 focus:bg-white focus:outline-none focus:ring-2 focus:ring-emerald-700/20"
              />
            </div>
          ))}

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="mb-1 block text-xs font-medium text-gray-700">Total copies</label>
              <input
                type="number"
                min={1}
                required
                {...field('totalCopies')}
                className="w-full rounded-lg border border-gray-200 bg-gray-50 px-3 py-2 text-sm text-gray-900 focus:border-emerald-700 focus:bg-white focus:outline-none focus:ring-2 focus:ring-emerald-700/20"
              />
            </div>
            <div>
              <label className="mb-1 block text-xs font-medium text-gray-700">Available copies</label>
              <input
                type="number"
                min={0}
                required
                {...field('availableCopies')}
                className="w-full rounded-lg border border-gray-200 bg-gray-50 px-3 py-2 text-sm text-gray-900 focus:border-emerald-700 focus:bg-white focus:outline-none focus:ring-2 focus:ring-emerald-700/20"
              />
            </div>
          </div>

          {error && <p className="text-sm text-red-600">{error}</p>}

          <div className="flex justify-end gap-3 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="rounded-lg border border-gray-200 px-4 py-2 text-sm font-semibold text-gray-700 transition hover:bg-gray-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={saving}
              className="rounded-lg bg-emerald-800 px-4 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-emerald-900 disabled:opacity-50"
            >
              {saving ? 'Saving…' : book ? 'Save changes' : 'Add book'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

// ── Page ──────────────────────────────────────────────────────────────────────

export function BookManagementPage() {
  const [books, setBooks] = useState<Book[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [search, setSearch] = useState('')
  const [categoryFilter, setCategoryFilter] = useState('')
  const [groupByCategory, setGroupByCategory] = useState(false)
  const [modalBook, setModalBook] = useState<Book | null>(null)
  const [showModal, setShowModal] = useState(false)

  useEffect(() => {
    setLoading(true)
    getBooks()
      .then(setBooks)
      .catch((err: unknown) => setError(err instanceof Error ? err.message : 'Failed to load books'))
      .finally(() => setLoading(false))
  }, [])

  const hasFilters = Boolean(search.trim() || categoryFilter)

  const categories = useMemo(() => {
    const set = new Set(books.flatMap((b) => b.categories))
    return Array.from(set).sort((a, b) => a.localeCompare(b))
  }, [books])

  const filteredBooks = useMemo(() => {
    return books.filter((book) => {
      if (!matchesSearch(book, search)) return false
      if (categoryFilter && !book.categories.includes(categoryFilter)) return false
      return true
    })
  }, [books, search, categoryFilter])

  const groupedBooks = useMemo(() => {
    const map = new Map<string, Book[]>()
    for (const book of filteredBooks) {
      for (const cat of book.categories) {
        const list = map.get(cat) ?? []
        list.push(book)
        map.set(cat, list)
      }
    }
    return Array.from(map.entries()).sort((a, b) => a[0].localeCompare(b[0]))
  }, [filteredBooks])

  function openAdd() {
    setModalBook(null)
    setShowModal(true)
  }

  function openEdit(book: Book) {
    setModalBook(book)
    setShowModal(true)
  }

  function closeModal() {
    setShowModal(false)
  }

  async function handleSave(req: BookRequest, id?: number) {
    if (id !== undefined) {
      const updated = await updateBook(id, req)
      setBooks((prev) => prev.map((b) => (b.id === id ? updated : b)))
    } else {
      const created = await createBook(req)
      setBooks((prev) => [created, ...prev])
    }
  }

  async function handleDelete(book: Book) {
    if (!window.confirm(`Delete "${book.title}"?`)) return
    try {
      await deleteBook(book.id)
      setBooks((prev) => prev.filter((b) => b.id !== book.id))
    } catch (err) {
      alert(err instanceof Error ? err.message : 'Failed to delete book')
    }
  }

  return (
    <DashboardShell roleLabel="Administrator" navItems={[...ADMIN_DASHBOARD_NAV]}>
      <div className="mx-auto max-w-6xl space-y-6">
        <div>
          <h1 className="text-2xl font-semibold text-gray-900">Book management</h1>
          <p className="mt-1 text-sm text-gray-600">
            Search, filter by category, or view titles grouped by category.
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
                  onClick={openAdd}
                  className="rounded-lg bg-emerald-800 px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition hover:bg-emerald-900"
                >
                  + Add book
                </button>
              </div>
            </div>

            {loading ? (
              <p className="py-8 text-center text-sm text-gray-500">Loading books…</p>
            ) : error ? (
              <p className="py-8 text-center text-sm text-red-600">{error}</p>
            ) : (
              <>
                <p className="text-sm text-gray-500">
                  Showing{' '}
                  <span className="font-medium text-gray-700">{filteredBooks.length}</span> of{' '}
                  {books.length} titles
                </p>

                {!groupByCategory ? (
                  <BookManagementTable
                    books={filteredBooks}
                    onEdit={openEdit}
                    onDelete={handleDelete}
                    hasFilters={hasFilters}
                  />
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
                        <BookManagementTable
                          books={sectionBooks}
                          onEdit={openEdit}
                          onDelete={handleDelete}
                          hasFilters={hasFilters}
                        />
                      </section>
                    ))}
                  </div>
                )}
              </>
            )}
          </div>
        </div>
      </div>

      {showModal && (
        <BookFormModal
          book={modalBook}
          onClose={closeModal}
          onSave={handleSave}
        />
      )}
    </DashboardShell>
  )
}
