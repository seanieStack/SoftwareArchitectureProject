import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { APP_ROUTES } from '../../../constants/routes'
import { WEBSITE_LOGO_SRC } from '../../../constants/assets'
import { getBooks } from '../../../http/bookService'
import { createBorrow } from '../../../http/supportService'
import type { Book } from '../../../types/catalog'
import { useAppSelector } from '../../../redux-features/store/hooks'

// ── Icons ────────────────────────────────────────────────────────────────────

function SearchIcon({ className }: { className?: string }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden>
      <circle cx="11" cy="11" r="8" />
      <path d="m21 21-4.35-4.35" strokeLinecap="round" />
    </svg>
  )
}

function BookOpenIcon({ className }: { className?: string }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" aria-hidden>
      <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z" strokeLinejoin="round" />
      <path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z" strokeLinejoin="round" />
    </svg>
  )
}

// ── Book card ─────────────────────────────────────────────────────────────────

function BookCard({
  book,
  onBorrow,
  borrowing,
}: {
  book: Book
  onBorrow: (book: Book) => void
  borrowing: boolean
}) {
  const available = book.availableCopies > 0

  return (
    <article className="flex flex-col rounded-2xl border border-gray-200/80 bg-white shadow-sm transition hover:shadow-md hover:-translate-y-0.5">
      {/* Spine colour strip */}
      <div className="h-1.5 w-full rounded-t-2xl bg-gradient-to-r from-emerald-600 to-emerald-400" aria-hidden />

      <div className="flex flex-1 flex-col gap-3 p-4">
        {/* Cover placeholder */}
        <div className="flex h-28 w-full items-center justify-center rounded-xl bg-emerald-50">
          <BookOpenIcon className="size-10 text-emerald-300" />
        </div>

        <div className="flex-1 space-y-1">
          <h2 className="line-clamp-2 text-sm font-semibold leading-snug text-gray-900">
            {book.title}
          </h2>
          <p className="line-clamp-1 text-xs text-gray-500">{book.authors.join(', ')}</p>
        </div>

        <div className="flex flex-wrap gap-1">
          {book.categories.map((cat) => (
            <span
              key={cat}
              className="inline-flex rounded-full border border-emerald-200 bg-emerald-50 px-2 py-0.5 text-[10px] font-medium text-emerald-900"
            >
              {cat}
            </span>
          ))}
        </div>

        <div className="flex items-center justify-between border-t border-gray-100 pt-3">
          <span
            className={`text-xs font-semibold ${available ? 'text-emerald-700' : 'text-red-500'}`}
          >
            {available ? `${book.availableCopies} available` : 'Unavailable'}
          </span>
          <button
            type="button"
            disabled={!available || borrowing}
            onClick={() => onBorrow(book)}
            className="rounded-lg bg-emerald-800 px-3 py-1.5 text-xs font-semibold text-white shadow-sm transition hover:bg-emerald-900 disabled:cursor-not-allowed disabled:opacity-40"
          >
            {borrowing ? 'Borrowing…' : 'Borrow'}
          </button>
        </div>
      </div>
    </article>
  )
}

// ── Page ──────────────────────────────────────────────────────────────────────

export function BrowseBooksPage() {
  const [books, setBooks] = useState<Book[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [search, setSearch] = useState('')
  const [categoryFilter, setCategoryFilter] = useState('')
  const [borrowingId, setBorrowingId] = useState<number | null>(null)

  const userId = useAppSelector((s) => s.user.profile?.id)

  useEffect(() => {
    getBooks()
      .then(setBooks)
      .catch((err: unknown) =>
        setError(err instanceof Error ? err.message : 'Failed to load books'),
      )
      .finally(() => setLoading(false))
  }, [])

  async function handleBorrow(book: Book) {
    if (!userId) return
    setBorrowingId(book.id)
    try {
      const deadline = new Date(Date.now() + 14 * 24 * 60 * 60 * 1000)
        .toISOString()
        .slice(0, 19)
      await createBorrow(userId, book.id, deadline)
      setBooks((prev) =>
        prev.map((b) =>
          b.id === book.id ? { ...b, availableCopies: b.availableCopies - 1 } : b,
        ),
      )
    } catch (err) {
      alert(err instanceof Error ? err.message : 'Failed to borrow book')
    } finally {
      setBorrowingId(null)
    }
  }

  const categories = useMemo(() => {
    const set = new Set(books.flatMap((b) => b.categories))
    return Array.from(set).sort((a, b) => a.localeCompare(b))
  }, [books])

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase()
    return books.filter((book) => {
      if (categoryFilter && !book.categories.includes(categoryFilter)) return false
      if (!q) return true
      return (
        book.title.toLowerCase().includes(q) ||
        book.authors.some((a) => a.toLowerCase().includes(q)) ||
        book.isbn.includes(q)
      )
    })
  }, [books, search, categoryFilter])

  return (
    <div className="min-h-screen bg-[#f6f7f9]">
      {/* Header */}
      <header className="sticky top-0 z-20 border-b border-gray-200/90 bg-white/95 backdrop-blur">
        <div className="flex w-full items-center justify-between gap-4 px-4 py-3 sm:px-6 lg:px-8">
          <img
            src={WEBSITE_LOGO_SRC}
            alt="UL E-Library"
            className="h-9 w-auto object-contain"
            width={120}
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

      <main className="w-full px-4 py-8 sm:px-6 lg:px-8">
        {/* Title + search bar */}
        <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900 md:text-3xl">Browse books</h1>
            {!loading && !error && (
              <p className="mt-1 text-sm text-gray-500">
                {filtered.length} of {books.length} titles
              </p>
            )}
          </div>

          <div className="flex flex-wrap gap-3">
            {/* Search */}
            <div className="relative">
              <SearchIcon className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-gray-400" />
              <input
                type="search"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Title, author, or ISBN…"
                className="w-64 rounded-lg border border-gray-200 bg-white py-2 pl-9 pr-3 text-sm text-gray-900 placeholder:text-gray-400 focus:border-emerald-700 focus:outline-none focus:ring-2 focus:ring-emerald-700/20"
                autoComplete="off"
              />
            </div>

            {/* Category filter */}
            <select
              value={categoryFilter}
              onChange={(e) => setCategoryFilter(e.target.value)}
              className="rounded-lg border border-gray-200 bg-white py-2 pl-3 pr-8 text-sm text-gray-900 focus:border-emerald-700 focus:outline-none focus:ring-2 focus:ring-emerald-700/20"
            >
              <option value="">All categories</option>
              {categories.map((c) => (
                <option key={c} value={c}>
                  {c}
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* States */}
        {loading && (
          <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6">
            {Array.from({ length: 12 }).map((_, i) => (
              <div
                key={i}
                className="h-64 animate-pulse rounded-2xl bg-gray-200"
                aria-hidden
              />
            ))}
          </div>
        )}

        {!loading && error && (
          <p className="rounded-xl border border-red-200 bg-red-50 px-4 py-6 text-center text-sm text-red-700">
            {error}
          </p>
        )}

        {!loading && !error && filtered.length === 0 && (
          <p className="rounded-xl border border-dashed border-gray-200 bg-white px-4 py-12 text-center text-sm text-gray-500">
            No books match your search.
          </p>
        )}

        {!loading && !error && filtered.length > 0 && (
          <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6">
            {filtered.map((book) => (
              <BookCard
                key={book.id}
                book={book}
                onBorrow={handleBorrow}
                borrowing={borrowingId === book.id}
              />
            ))}
          </div>
        )}
      </main>
    </div>
  )
}
