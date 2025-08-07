export interface ImportLog {
  id: number
  timestamp: string // ISO date string
  supplierId: number
  supplierName: string
  status: "SUCCESS" | "FAILED" | "PARTIAL"
  productsProcessed: number
  errors: number
  errorMessage: string | null
}
