import { API_PATHS } from '../constants/api'
import { authorizedFetch } from './client'
import type { Book, BookAvailability, BookRequest } from '../types/catalog'

async function parseErrorResponse(res: Response): Promise<string> {
  const text = await res.text()
  if (!text) return res.statusText || 'Request failed'
  try {
    const body = JSON.parse(text) as { message?: string; error?: string }
    return body.message ?? body.error ?? text
  } catch {
    return text
  }
}

export async function getBooks(): Promise<Book[]> {
  const res = await authorizedFetch(API_PATHS.BOOKS.LIST)
  if (!res.ok) throw new Error(await parseErrorResponse(res))
  return res.json() as Promise<Book[]>
}

export type BookSearchParams = {
  keyword?: string
  author?: string
  category?: string
}

export async function searchBooks(params: BookSearchParams): Promise<Book[]> {
  const query = new URLSearchParams()
  if (params.keyword) query.set('keyword', params.keyword)
  if (params.author) query.set('author', params.author)
  if (params.category) query.set('category', params.category)
  const path = `${API_PATHS.BOOKS.SEARCH}?${query.toString()}`
  const res = await authorizedFetch(path)
  if (!res.ok) throw new Error(await parseErrorResponse(res))
  return res.json() as Promise<Book[]>
}

export async function getBook(bookId: number): Promise<Book> {
  const res = await authorizedFetch(API_PATHS.BOOKS.BY_ID(bookId))
  if (!res.ok) throw new Error(await parseErrorResponse(res))
  return res.json() as Promise<Book>
}

export async function getBookAvailability(bookId: number): Promise<BookAvailability> {
  const res = await authorizedFetch(API_PATHS.BOOKS.AVAILABILITY(bookId))
  if (!res.ok) throw new Error(await parseErrorResponse(res))
  return res.json() as Promise<BookAvailability>
}

export async function createBook(body: BookRequest): Promise<Book> {
  const res = await authorizedFetch(API_PATHS.BOOKS.LIST, {
    method: 'POST',
    body: JSON.stringify(body),
  })
  if (!res.ok) throw new Error(await parseErrorResponse(res))
  return res.json() as Promise<Book>
}

export async function updateBook(bookId: number, body: BookRequest): Promise<Book> {
  const res = await authorizedFetch(API_PATHS.BOOKS.BY_ID(bookId), {
    method: 'PUT',
    body: JSON.stringify(body),
  })
  if (!res.ok) throw new Error(await parseErrorResponse(res))
  return res.json() as Promise<Book>
}

export async function deleteBook(bookId: number): Promise<void> {
  const res = await authorizedFetch(API_PATHS.BOOKS.BY_ID(bookId), { method: 'DELETE' })
  if (!res.ok) throw new Error(await parseErrorResponse(res))
}

/** Retire all available copies. If no copies are borrowed, the book is deleted entirely. */
export async function retireBook(bookId: number): Promise<Book | null> {
  const res = await authorizedFetch(API_PATHS.BOOKS.RETIRE(bookId), { method: 'PATCH' })
  if (!res.ok) throw new Error(await parseErrorResponse(res))
  if (res.status === 204) return null
  return res.json() as Promise<Book>
}
