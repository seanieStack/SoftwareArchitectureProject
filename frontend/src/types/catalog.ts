/** Matches core-service BookDTO */
export type Book = {
  id: number
  title: string
  isbn: string
  authors: string[]
  categories: string[]
  totalCopies: number
  availableCopies: number
  active: boolean
  createdAt: string
  updatedAt: string
}

/** Matches core-service BookAvailabilityDTO */
export type BookAvailability = {
  bookId: number
  title: string
  available: boolean
  availableCopies: number
}

/** Body for POST /api/books and PUT /api/books/{id} */
export type BookRequest = {
  title: string
  isbn: string
  authors: string[]
  categories: string[]
  totalCopies: number
  availableCopies: number
}
