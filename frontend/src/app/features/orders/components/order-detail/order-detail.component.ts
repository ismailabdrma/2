import { Component, OnInit, inject } from "@angular/core"
import { CommonModule, CurrencyPipe, DatePipe } from "@angular/common"
import { MatCardModule } from "@angular/material/card"
import { MatButtonModule } from "@angular/material/button"
import { MatIconModule } from "@angular/material/icon"
import { MatListModule } from "@angular/material/list"
import { MatDividerModule } from "@angular/material/divider"
import { ActivatedRoute, RouterLink } from "@angular/router"
import { MatSnackBar } from "@angular/material/snack-bar"
import { OrderService } from "../../../../core/services/order.service"
import { PaymentService } from "../../../../core/services/payment.service"
import { Order } from "../../../../core/models/order.model"

@Component({
  selector: "app-order-detail",
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
  templateUrl: "./order-detail.component.html",
  styleUrl: "./order-detail.component.scss",
})
export class OrderDetailComponent implements OnInit {
  order: Order | null = null
  error: string | null = null

  private route = inject(ActivatedRoute)
  private orderService = inject(OrderService)
  private paymentService = inject(PaymentService)
  private snackBar = inject(MatSnackBar)

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      const orderId = Number(params.get("id"))
      if (orderId) {
        this.loadOrderDetails(orderId)
      } else {
        this.error = "Order ID not provided."
      }
    })
  }

  loadOrderDetails(orderId: number): void {
    this.orderService.getOrderById(orderId).subscribe({
      next: (order) => {
        this.order = order
      },
      error: (err) => {
        console.error("Error fetching order details:", err)
        this.error = `Failed to load order details: ${err.error?.message || err.message}`
        this.snackBar.open(this.error, "Close", { duration: 5000 })
      },
    })
  }

  retryPayment(orderId: number): void {
    this.paymentService.createCheckoutSession(orderId).subscribe({
      next: (session) => {
        window.location.href = session.url // Redirect to Stripe Checkout
      },
      error: (err) => {
        console.error("Error retrying payment:", err)
        this.snackBar.open(`Failed to retry payment: ${err.error?.message || err.message}`, "Close", {
          duration: 5000,
        })
      },
    })
  }
}
