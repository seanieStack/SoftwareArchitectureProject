import type { Book } from '../../../types/catalog'
import { PencilIcon, TrashIcon } from '../utils/adminIcons'

type BookManagementTableProps = {
  books: Book[]
  onEdit?: (book: Book) => void
  onDelete?: (book: Book) => void
  /** When true, empty results are treated as "no match" rather than an empty catalog. */
  hasFilters?: boolean
}

export function BookManagementTable({ books, onEdit, onDelete, hasFilters }: BookManagementTableProps) {
  if (books.length === 0) {
    return (
      <p className="rounded-lg border border-dashed border-gray-200 bg-gray-50/80 px-4 py-8 text-center text-sm text-gray-500">
        {hasFilters
          ? 'No books match your search or filters.'
          : 'No books in the catalog yet.'}
      </p>
    )
  }

  return (
    <div className="max-h-[min(70vh,720px)] overflow-auto rounded-lg border border-gray-100">
      <table className="w-full min-w-[720px] border-collapse text-left text-sm">
        <thead className="sticky top-0 z-10 bg-white shadow-[0_1px_0_0_rgb(229_231_235)]">
          <tr className="text-xs font-semibold uppercase tracking-wide text-gray-500">
            <th className="pb-3 pl-0 pr-4 pt-1">Book</th>
            <th className="pb-3 pr-4 pt-1">Category</th>
            <th className="pb-3 pr-4 pt-1">ISBN</th>
            <th className="pb-3 pr-4 pt-1">Available</th>
            <th className="pb-3 pr-4 pt-1">Total</th>
            <th className="pb-3 pr-0 pt-1 text-right">Actions</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-100">
          {books.map((book) => (
            <tr key={book.id} className="align-middle">
              <td className="py-4 pr-4">
                <div className="flex items-start gap-3">
                  <div
                    className="flex h-14 w-10 shrink-0 items-center justify-center rounded bg-gray-100 text-xs font-semibold text-gray-600"
                    aria-hidden
                  >
                    {book.title.slice(0, 1)}
                  </div>
                  <div className="min-w-0">
                    <p className="font-semibold text-gray-900">{book.title}</p>
                    <p className="text-sm text-gray-500">{book.authors.join(', ')}</p>
                  </div>
                </div>
              </td>
              <td className="py-4 pr-4">
                <div className="flex flex-wrap gap-1">
                  {book.categories.map((cat) => (
                    <span
                      key={cat}
                      className="inline-flex rounded-full border border-emerald-200 bg-emerald-50 px-2.5 py-0.5 text-xs font-medium text-emerald-900"
                    >
                      {cat}
                    </span>
                  ))}
                </div>
              </td>
              <td className="py-4 pr-4 font-mono text-gray-700">{book.isbn}</td>
              <td className="py-4 pr-4">
                <span
                  className={
                    book.availableCopies > 0
                      ? 'font-semibold text-emerald-700'
                      : 'font-semibold text-red-600'
                  }
                >
                  {book.availableCopies}
                </span>
              </td>
              <td className="py-4 pr-4 text-gray-600">{book.totalCopies}</td>
              <td className="py-4 pl-2 text-right">
                <div className="inline-flex items-center justify-end gap-1">
                  <button
                    type="button"
                    onClick={() => onEdit?.(book)}
                    className="rounded-lg p-2 text-gray-500 transition hover:bg-gray-100 hover:text-emerald-900"
                    aria-label={`Edit ${book.title}`}
                  >
                    <PencilIcon className="size-4" />
                  </button>
                  <button
                    type="button"
                    onClick={() => onDelete?.(book)}
                    className="rounded-lg p-2 text-red-500 transition hover:bg-red-50 hover:text-red-700"
                    aria-label={`Delete ${book.title}`}
                  >
                    <TrashIcon className="size-4" />
                  </button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
