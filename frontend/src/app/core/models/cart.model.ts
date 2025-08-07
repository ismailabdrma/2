export interface CartItem {
  id: number
  productId: number
  productName: string
  quantity: number
  unitPrice: number
  imageUrl: string | null
}

export interface Cart {
  id: number
  clientId: number
  items: CartItem[]
  totalItems: number
  totalAmount: number
}

export interface AddToCartRequest {
  productId: number
  quantity: number
}
