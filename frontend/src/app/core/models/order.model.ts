import {Address} from "./user.model";

export interface OrderItem {
  id: number
  productId: number
  productName: string
  quantity: number
  unitPrice: number
  imageUrl: string | null
}

export interface Order {
  id: number
  clientId: number
  orderDate: string // ISO date string
  totalAmount: number
  shippingFee: number
  status: "PENDING" | "PROCESSING" | "SHIPPED" | "DELIVERED" | "CANCELLED"
  deliveryStatus: "PENDING" | "SHIPPED" | "DELIVERED" | "RETURNED"
  shippingAddress: Address // Assuming Address is defined in user.model.ts
  orderItems: OrderItem[]
  paymentStatus: "PENDING" | "COMPLETED" | "FAILED" | "REFUNDED"
}
export interface OrderSummary {
  id: number
  orderDate: string // ISO date string
  totalAmount: number
  shippingFee: number
  status: "PENDING" | "PROCESSING" | "SHIPPED" | "DELIVERED" | "CANCELLED"
  deliveryStatus: "PENDING" | "SHIPPED" | "DELIVERED" | "RETURNED"
}

export interface CreateOrderRequest {
  addressId: number
  shippingFee: number
}
