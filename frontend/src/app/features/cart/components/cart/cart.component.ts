import { Component, OnInit, inject } from "@angular/core"
import { CommonModule, CurrencyPipe } from "@angular/common"
import { MatCardModule } from "@angular/material/card"
import { MatButtonModule } from "@angular/material/button"
import { MatIconModule } from "@angular/material/icon"
import { MatListModule } from "@angular/material/list"
import { MatDividerModule } from "@angular/material/divider"
import { RouterLink } from "@angular/router"
import { MatSnackBar } from "@angular/material/snack-bar"
import { CartService } from "../../../core/services/cart.service"
import { Cart } from "../../../core/models/cart.model"

@Component({
  selector: "app-cart",
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatListModule,
    MatDividerModule,
    RouterLink,
    CurrencyPipe,
  ],
  templateUrl: "./cart.component.html",
  styleUrl: "./cart.component.scss",
})
export class CartComponent implements OnInit {
  cart: Cart | null = null

  private cartService = inject(CartService)
  private snackBar = inject(MatSnackBar)

  ngOnInit(): void {
    this.loadCart()
  }

  loadCart(): void {
    this.cartService.getCart().subscribe({
      next: (cart) => {
        this.cart = cart
      },
      error: (err) => {
        console.error("Error loading cart:", err)
        this.snackBar.open("Failed to load cart. Please try again.", "Close", { duration: 3000 })
      },
    })
  }

  updateQuantity(productId: number, newQuantity: number): void {
    if (newQuantity < 1) {
      this.removeItem(productId)
      return
    }

    this.cartService.updateCartItem(productId, newQuantity).subscribe({
      next: (cart) => {
        this.cart = cart
        this.snackBar.open("Cart updated!", "Close", { duration: 1500 })
      },
      error: (err) => {
        console.error("Error updating cart item quantity:", err)
        this.snackBar.open("Failed to update item quantity.", "Close", { duration: 3000 })
      },
    })
  }

  removeItem(productId: number): void {
    this.cartService.removeCartItem(productId).subscribe({
      next: (cart) => {
        this.cart = cart
        this.snackBar.open("Item removed from cart.", "Close", { duration: 1500 })
      },
      error: (err) => {
        console.error("Error removing cart item:", err)
        this.snackBar.open("Failed to remove item from cart.", "Close", { duration: 3000 })
      },
    })
  }

  clearCart(): void {
    this.cartService.clearCart().subscribe({
      next: () => {
        this.cart = { id: 0, clientId: 0, items: [], totalItems: 0, totalAmount: 0 } // Reset cart
        this.snackBar.open("Cart cleared successfully!", "Close", { duration: 3000 })
      },
      error: (err) => {
        console.error("Error clearing cart:", err)
        this.snackBar.open("Failed to clear cart.", "Close", { duration: 3000 })
      },
    })
  }
}
