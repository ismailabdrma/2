import { Component, Input, inject } from "@angular/core"
import { CommonModule, CurrencyPipe } from "@angular/common"
import { MatCardModule } from "@angular/material/card"
import { MatButtonModule } from "@angular/material/button"
import { MatIconModule } from "@angular/material/icon"
import { Router } from "@angular/router"
import { MatSnackBar } from "@angular/material/snack-bar"
import { Product } from "../../../core/models/product.model"
import { CartService } from "../../../core/services/cart.service"

@Component({
  selector: "app-product-card",
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, MatIconModule, CurrencyPipe],
  templateUrl: "./product-card.component.html",
  styleUrl: "./product-card.component.scss",
})
export class ProductCardComponent {
  @Input() product!: Product

  private router = inject(Router)
  private cartService = inject(CartService)
  private snackBar = inject(MatSnackBar)

  navigateToProductDetail(): void {
    this.router.navigate(["/products", this.product.id])
  }

  addToCart(event: Event): void {
    event.stopPropagation() // Prevent navigating to detail page
    const stock = this.product.syncedStock ?? 0
    if (stock > 0) {
      this.cartService.addItemToCart({ productId: this.product.id, quantity: 1 }).subscribe({
        next: () => {
          this.snackBar.open(`${this.product.name} added to cart!`, "Close", { duration: 2000 })
        },
        error: (err: unknown) => {
          console.error("Error adding to cart:", err)
          this.snackBar.open(`Failed to add ${this.product.name} to cart.`, "Close", {
            duration: 3000,
          })
        },
      })
    } else {
      this.snackBar.open("Product is out of stock.", "Close", { duration: 2000 })
    }
  }
}
