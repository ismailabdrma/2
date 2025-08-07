import { Component, OnInit, inject } from "@angular/core"
import { CommonModule, CurrencyPipe } from "@angular/common"
import { MatCardModule } from "@angular/material/card"
import { MatButtonModule } from "@angular/material/button"
import { MatIconModule } from "@angular/material/icon"
import { MatFormFieldModule } from "@angular/material/form-field"
import { MatInputModule } from "@angular/material/input"
import { FormsModule } from "@angular/forms"
import { ActivatedRoute, RouterLink } from "@angular/router"
import { MatSnackBar } from "@angular/material/snack-bar"
import { ProductService } from "../../../../core/services/product.service"
import { CartService } from "../../../../core/services/cart.service"
import { Product } from "../../../../core/models/product.model"
import { ProductCardComponent } from "../../../../shared/components/product-card/product-card.component"

@Component({
  selector: "app-product-detail",
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    FormsModule,
    RouterLink,
    CurrencyPipe,
    ProductCardComponent,
  ],
  templateUrl: "./product-detail.component.html",
  styleUrl: "./product-detail.component.scss",
})
export class ProductDetailComponent implements OnInit {
  product: Product | null = null
  suggestedProducts: Product[] = []
  quantity: number = 1
  mainImageUrl: string | null = null
  error: string | null = null

  private route = inject(ActivatedRoute)
  private productService = inject(ProductService)
  private cartService = inject(CartService)
  private snackBar = inject(MatSnackBar)

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      const productId = Number(params.get("id"))
      if (productId) {
        this.loadProductDetails(productId)
        this.loadSuggestedProducts(productId)
      } else {
        this.error = "Product ID not provided."
      }
    })
  }

  loadProductDetails(id: number): void {
    this.productService.getProductById(id).subscribe({
      next: (product) => {
        this.product = product
        this.mainImageUrl = product.imageUrls?.[0] || null
        this.quantity = product.syncedStock > 0 ? 1 : 0 // Default quantity
      },
      error: (err) => {
        console.error("Error fetching product details:", err)
        this.error = `Failed to load product details: ${err.error?.message || err.message}`
        this.snackBar.open(this.error, "Close", { duration: 5000 })
      },
    })
  }

  loadSuggestedProducts(productId: number): void {
    this.productService.getSuggestedProducts(productId).subscribe({
      next: (products) => {
        this.suggestedProducts = products
      },
      error: (err) => {
        console.error("Error fetching suggested products:", err)
        // Not critical, so just log and don't show snackbar
      },
    })
  }

  setMainImage(url: string): void {
    this.mainImageUrl = url
  }

  onQuantityChange(): void {
    if (this.product) {
      if (this.quantity < 1) {
        this.quantity = 1
      } else if (this.quantity > this.product.syncedStock) {
        this.quantity = this.product.syncedStock
      }
    }
  }

  addToCart(): void {
    if (this.product && this.quantity > 0 && this.quantity <= this.product.syncedStock) {
      this.cartService.addToCart(this.product.id, this.quantity).subscribe({
        next: () => {
          this.snackBar.open(`${this.quantity} x ${this.product?.name} added to cart!`, "Close", {
            duration: 3000,
          })
        },
        error: (err) => {
          console.error("Error adding to cart:", err)
          this.snackBar.open(`Failed to add to cart: ${err.error?.message || err.message}`, "Close", {
            duration: 3000,
          })
        },
      })
    } else {
      this.snackBar.open("Please enter a valid quantity.", "Close", { duration: 3000 })
    }
  }
}
