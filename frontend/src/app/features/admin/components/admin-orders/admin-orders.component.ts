import { Component, type OnInit, inject } from "@angular/core"
import { CommonModule } from "@angular/common"
import { RouterModule } from "@angular/router"
import { MatCardModule } from "@angular/material/card"
import { MatTableModule } from "@angular/material/table"
import { MatPaginatorModule } from "@angular/material/paginator"
import { MatSortModule } from "@angular/material/sort"
import { MatFormFieldModule } from "@angular/material/form-field"
import { MatInputModule } from "@angular/material/input"
import { MatButtonModule } from "@angular/material/button"
import { MatIconModule } from "@angular/material/icon"
import { MatChipsModule } from "@angular/material/chips"
import { MatSnackBar } from "@angular/material/snack-bar"
import { OrderService } from "@core/services/order.service"
import type { Order } from "@core/models/order.model"

@Component({
  selector: "app-admin-orders",
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatSnackBar,
  ],
  templateUrl: "./admin-orders.component.html",
  styleUrl: "./admin-orders.component.scss",
})
export class AdminOrdersComponent implements OnInit {
  private snackBar = inject(MatSnackBar)
  private orderService = inject(OrderService)

  orders: Order[] = []
  displayedColumns: string[] = ["id", "customerName", "orderDate", "total", "status", "actions"]

  ngOnInit(): void {
    this.loadOrders()
  }

  loadOrders(): void {
    this.orderService.getAllOrders().subscribe({
      next: (orders) => {
        this.orders = orders
      },
      error: (error) => {
        console.error("Error loading orders:", error)
 this.snackBar.open("Failed to load orders.", "Close", { duration: 3000 })
        this.orders = []
      },
    })
  }

  getStatusColor(status: string): string {
    switch (status) {
      case "PENDING":
        return "warn"
      case "CONFIRMED":
        return "primary"
      case "SHIPPED":
        return "accent"
      case "DELIVERED":
        return "primary"
      case "CANCELLED":
        return "warn"
      default:
        return ""
    }
  }

  updateOrderStatus(order: Order, status: string): void {
    this.orderService.updateOrderStatus(order.id, status).subscribe({
      next: (updatedOrder) => {
        this.snackBar.open(`Order #${updatedOrder.id} status updated to ${updatedOrder.status}`, "Close", { duration: 3000 })
        this.loadOrders() // Refresh the list
      },
      error: (error) => {
        console.error("Error updating order status:", error)
 this.snackBar.open("Failed to update order status.", "Close", { duration: 3000 })
      },
    })
  }
}
