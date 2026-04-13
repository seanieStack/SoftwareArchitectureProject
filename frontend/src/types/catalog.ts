/** Admin catalogue row. */
export type AdminBookRow = {
  id: string
  title: string
  author: string
  category: string
  isbn: string
  available: number
  total: number
  coverSrc?: string
}
