import { useEffect, useRef, useState } from 'react'
import type { Book, BookRequest } from '../../../types/catalog'

type BookFormModalProps = {
  book?: Book | null
  onSave: (data: BookRequest) => Promise<void>
  onClose: () => void
}

export function BookFormModal({ book, onSave, onClose }: BookFormModalProps) {
  const isEditing = Boolean(book)

  const [title, setTitle] = useState(book?.title ?? '')
  const [isbn, setIsbn] = useState(book?.isbn ?? '')
  const [authorsRaw, setAuthorsRaw] = useState(book?.authors.join(', ') ?? '')
  const [categoriesRaw, setCategoriesRaw] = useState(book?.categories.join(', ') ?? '')
  const [totalCopies, setTotalCopies] = useState(book?.totalCopies ?? 1)
  const [availableCopies, setAvailableCopies] = useState(book?.availableCopies ?? 1)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const titleRef = useRef<HTMLInputElement>(null)
  useEffect(() => titleRef.current?.focus(), [])

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError(null)

    const authors = authorsRaw.split(',').map((s) => s.trim()).filter(Boolean)
    const categories = categoriesRaw.split(',').map((s) => s.trim()).filter(Boolean)

    if (!title.trim() || !isbn.trim() || authors.length === 0) {
      setError('Title, ISBN, and at least one author are required.')
      return
    }

    if (availableCopies > totalCopies) {
      setError('Available copies cannot exceed total copies.')
      return
    }

    setSaving(true)
    try {
      await onSave({ title: title.trim(), isbn: isbn.trim(), authors, categories, totalCopies, availableCopies })
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to save book')
      setSaving(false)
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div className="absolute inset-0 bg-black/40" onClick={onClose} />
      <form
        onSubmit={handleSubmit}
        className="relative z-10 w-full max-w-lg rounded-xl border border-gray-200 bg-white p-6 shadow-xl"
      >
        <h2 className="text-lg font-semibold text-gray-900">
          {isEditing ? 'Edit book' : 'Add book'}
        </h2>

        <div className="mt-4 space-y-4">
          <Field label="Title">
            <input
              ref={titleRef}
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              className="w-full rounded-lg border border-gray-200 bg-gray-50 px-3 py-2.5 text-sm text-gray-900 placeholder:text-gray-400 focus:border-emerald-700 focus:bg-white focus:outline-none focus:ring-2 focus:ring-emerald-700/20"
            />
          </Field>

          <Field label="ISBN">
            <input
              type="text"
              value={isbn}
              onChange={(e) => setIsbn(e.target.value)}
              className="w-full rounded-lg border border-gray-200 bg-gray-50 px-3 py-2.5 text-sm text-gray-900 placeholder:text-gray-400 focus:border-emerald-700 focus:bg-white focus:outline-none focus:ring-2 focus:ring-emerald-700/20"
            />
          </Field>

          <Field label="Authors" hint="Comma-separated">
            <input
              type="text"
              value={authorsRaw}
              onChange={(e) => setAuthorsRaw(e.target.value)}
              placeholder="Author One, Author Two"
              className="w-full rounded-lg border border-gray-200 bg-gray-50 px-3 py-2.5 text-sm text-gray-900 placeholder:text-gray-400 focus:border-emerald-700 focus:bg-white focus:outline-none focus:ring-2 focus:ring-emerald-700/20"
            />
          </Field>

          <Field label="Categories" hint="Comma-separated">
            <input
              type="text"
              value={categoriesRaw}
              onChange={(e) => setCategoriesRaw(e.target.value)}
              placeholder="Fiction, Science"
              className="w-full rounded-lg border border-gray-200 bg-gray-50 px-3 py-2.5 text-sm text-gray-900 placeholder:text-gray-400 focus:border-emerald-700 focus:bg-white focus:outline-none focus:ring-2 focus:ring-emerald-700/20"
            />
          </Field>

          <div className="grid grid-cols-2 gap-4">
            <Field label="Total copies">
              <input
                type="number"
                min={0}
                value={totalCopies}
                onChange={(e) => setTotalCopies(Number(e.target.value))}
                className="w-full rounded-lg border border-gray-200 bg-gray-50 px-3 py-2.5 text-sm text-gray-900 placeholder:text-gray-400 focus:border-emerald-700 focus:bg-white focus:outline-none focus:ring-2 focus:ring-emerald-700/20"
              />
            </Field>
            <Field label="Available copies">
              <input
                type="number"
                min={0}
                max={totalCopies}
                value={availableCopies}
                onChange={(e) => setAvailableCopies(Number(e.target.value))}
                className="w-full rounded-lg border border-gray-200 bg-gray-50 px-3 py-2.5 text-sm text-gray-900 placeholder:text-gray-400 focus:border-emerald-700 focus:bg-white focus:outline-none focus:ring-2 focus:ring-emerald-700/20"
              />
            </Field>
          </div>
        </div>

        {error && <p className="mt-3 text-sm text-red-600">{error}</p>}

        <div className="mt-6 flex items-center justify-end gap-3">
          <button
            type="button"
            onClick={onClose}
            className="rounded-lg border border-gray-200 px-4 py-2 text-sm font-medium text-gray-700 transition hover:bg-gray-50"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={saving}
            className="rounded-lg bg-emerald-800 px-4 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-emerald-900 disabled:opacity-50"
          >
            {saving ? 'Saving...' : isEditing ? 'Save changes' : 'Add book'}
          </button>
        </div>
      </form>
    </div>
  )
}

function Field({ label, hint, children }: { label: string; hint?: string; children: React.ReactNode }) {
  return (
    <label className="block">
      <span className="mb-1 block text-xs font-medium uppercase tracking-wide text-gray-500">
        {label}
        {hint && <span className="ml-1 normal-case tracking-normal text-gray-400">({hint})</span>}
      </span>
      {children}
    </label>
  )
}
