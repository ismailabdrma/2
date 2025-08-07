import { Injectable, inject } from "@angular/core"
import { HttpClient } from "@angular/common/http"
import type { Observable } from "rxjs"
import { environment } from '../../../environments/environment'
import type { Order, CreateOrderRequest, OrderSummary } from "../models/order.model"

@Injectable({
  providedIn: "root",
})
export class OrderService {
  private http = inject(HttpClient)
  private apiUrl = `${environment.apiUrl}/api/orders`

  getOrders(): Observable<OrderSummary[]> {
    return this.http.get<OrderSummary[]>(this.apiUrl)
  }

  getOrderById(id: number): Observable<Order> {
    return this.http.get<Order>(`${this.apiUrl}/${id}`)
  }

  createOrder(request: CreateOrderRequest): Observable<Order> {
    return this.http.post<Order>(this.apiUrl, request)
  }

  updateOrderStatus(id: number, status: string): Observable<Order> {
    return this.http.put<Order>(`${this.apiUrl}/${id}/status`, { status })
  }

  cancelOrder(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`)
  }

  // Admin methods
  getAllOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.apiUrl}/admin/all`)
  }

  getOrdersByStatus(status: string): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.apiUrl}/admin/status/${status}`)
  }
}
