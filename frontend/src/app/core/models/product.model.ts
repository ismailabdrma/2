export interface Product {
  id: number
  name: string
  description: string
  displayedPrice: number
  syncedStock?: number
  dynamicPrice?: number
  lastFetched?: string
  imageUrls: string[]
  categoryName: string
  supplierName: string
  externalProductId?: string
  isActive: boolean
}

export interface Category {
  id: number
  name: string
  description: string
  active: boolean
}
