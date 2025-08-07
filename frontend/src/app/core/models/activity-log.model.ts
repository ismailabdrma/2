export interface ActivityLog {
  id: number
  timestamp: string // ISO date string
  username: string
  actionType: string
  description: string
}

export interface PaginatedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number // current page number
  first: boolean
  last: boolean
}
