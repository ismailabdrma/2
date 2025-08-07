import { Injectable, inject } from "@angular/core"
import { HttpClient } from "@angular/common/http"
import type { Observable } from "rxjs"
import { environment } from "../../../environments/environment"
import type { Cart, CartItem } from "../models/cart.model"

@Injectable({
  providedIn: "root",
})
export class CartService {
  private http = inject(HttpClient)
  private apiUrl = `${environment.apiUrl}/api/cart`

  getCart(): Observable<Cart> {
    return this.http.get<Cart>(this.apiUrl)
  }

  addItemToCart(item: Partial<CartItem>): Observable<Cart> {
    return this.http.post<Cart>(`${this.apiUrl}/add`, item)
  }

  updateItemQuantity(cartItemId: number, quantity: number): Observable<Cart> {
    return this.http.put<Cart>(`${this.apiUrl}/item/${cartItemId}`, null, {
      params: { quantity: quantity.toString() },
    })
  }

  removeItemFromCart(cartItemId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/item/${cartItemId}`)
  }

  clearCart(): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/clear`)
  }
}
