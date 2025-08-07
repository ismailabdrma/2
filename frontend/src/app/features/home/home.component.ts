import { Component, OnInit, inject } from "@angular/core"
import { CommonModule } from "@angular/common"
import { MatCardModule } from "@angular/material/card"
import { MatButtonModule } from "@angular/material/button"
import { RouterLink } from "@angular/router"
import { ProductCardComponent } from "../../shared/components/product-card/product-card.component"
import { ProductService } from "../../core/services/product.service"
import { Product } from "../../core/models/product.model"
import { MatSnackBar } from "@angular/material/snack-bar"

@Component({
  selector: "app-home",
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, RouterLink, ProductCardComponent],
  templateUrl: "./home.component.html",
  styleUrl: "./home.component.scss",
})
export class HomeComponent implements OnInit {
  featuredProducts: Product[] = []

  private productService = inject(ProductService)
  private snackBar = inject(MatSnackBar)

  ngOnInit(): void {
    this.loadFeaturedProducts()
  }

  loadFeaturedProducts(): void {
    this.productService.getFeaturedProducts().subscribe({
      next: (products) => {
        this.featuredProducts = products
      },
      error: (err) => {
        console.error("Error fetching featured products:", err)
        this.snackBar.open("Failed to load featured products.", "Close", { duration: 3000 })
      },
    })
  }
}
