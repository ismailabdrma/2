import { Component, OnInit, inject } from "@angular/core"
import { CommonModule, CurrencyPipe, DatePipe } from "@angular/common"
import { MatCardModule } from "@angular/material/card"
import { MatButtonModule } from "@angular/material/button"
import { MatIconModule } from "@angular/material/icon"
import { MatListModule } from "@angular/material/list"
import { MatDividerModule } from "@angular/material/divider"
import { RouterLink } from "@angular/router"
import { MatSnackBar } from "@angular/material/snack-bar"
import { OrderService } from "../../../../core/services/order.service"
import { Order } from "../../../../core/models/order.model"

@Component({
  selector: "app-order-list",
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
    DatePipe,
  ],
  templateUrl: "./order-list.component.html",
  styleUrl: "./order-list.component.scss",
})
export class OrderListComponent implements OnInit {
  orders: Order[] = []

  private orderService = inject(OrderService)
  private snackBar = inject(MatSnackBar)

  ngOnInit(): void {
    this.loadOrders()
  }

  loadOrders(): void {
    this.orderService.getClientOrders().subscribe({
      next: (orders) => {
        this.orders = orders
      },
      error: (err) => {
        console.error("Error fetching orders:", err)
        this.snackBar.open("Failed to load your orders. Please try again.", "Close", {
          duration: 3000,
        })
      },
    })
  }
}
