import { Component, OnInit, inject } from "@angular/core"
import { CommonModule, CurrencyPipe } from "@angular/common"
import { MatCardModule } from "@angular/material/card"
import { MatButtonModule } from "@angular/material/button"
import { MatIconModule } from "@angular/material/icon"
import { MatListModule } from "@angular/material/list"
import { MatDividerModule } from "@angular/material/divider"
import { MatFormFieldModule } from "@angular/material/form-field"
import { MatSelectModule } from "@angular/material/select"
import { FormControl, ReactiveFormsModule, Validators } from "@angular/forms"
import { Router, RouterLink } from "@angular/router"
import { MatSnackBar } from "@angular/material/snack-bar"
import { CartService } from "../../../../core/services/cart.service"
import { OrderService } from "../../../../core/services/order.service"
import { PaymentService } from "../../../../core/services/payment.service"
import { AddressService } from "../../../../core/services/address.service"
import { Cart } from "../../../../core/models/cart.model"
import { Address } from "../../../../core/models/user.model"

@Component({
  selector: "app-checkout",
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatListModule,
    MatDividerModule,
    MatFormFieldModule,
    MatSelectModule,
    ReactiveFormsModule,
    RouterLink,
    CurrencyPipe,
  ],
  templateUrl: "./checkout.component.html",
  styleUrl: "./checkout.component.scss",
})
export class CheckoutComponent implements OnInit {
  cart: Cart | null = null
  addresses: Address[] = []
  selectedAddressControl = new FormControl<number | null>(null, Validators.required)
  shippingFee: number = 5.0 // Example fixed shipping fee
  isPlacingOrder = false

  private cartService = inject(CartService)
  private orderService = inject(OrderService)
  private paymentService = inject(PaymentService)
  private addressService = inject(AddressService)
  private snackBar = inject(MatSnackBar)
  private router = inject(Router)

  ngOnInit(): void {
    this.loadCart()
    this.loadAddresses()
  }

  loadCart(): void {
    this.cartService.getCart().subscribe({
      next: (cart) => {
        this.cart = cart
        if (!cart || cart.items.length === 0) {
          this.snackBar.open("Your cart is empty. Redirecting to products.", "Close", {
            duration: 3000,
          })
          this.router.navigate(["/products"])
        }
      },
      error: (err) => {
        console.error("Error loading cart:", err)
        this.snackBar.open("Failed to load cart. Please try again.", "Close", { duration: 3000 })
        this.router.navigate(["/cart"])
      },
    })
  }

  loadAddresses(): void {
    this.addressService.getAddresses().subscribe({
      next: (addresses) => {
        this.addresses = addresses
        const defaultAddress = addresses.find((addr) => addr.isDefault)
        if (defaultAddress) {
          this.selectedAddressControl.setValue(defaultAddress.id)
        }
      },
      error: (err) => {
        console.error("Error loading addresses:", err)
        this.snackBar.open("Failed to load addresses. Please add one in your profile.", "Close", {
          duration: 3000,
        })
      },
    })
  }

  placeOrder(): void {
    if (this.selectedAddressControl.invalid || !this.cart || this.cart.items.length === 0) {
      this.snackBar.open("Please select a valid address and ensure your cart is not empty.", "Close", {
        duration: 3000,
      })
      return
    }

    this.isPlacingOrder = true
    const selectedAddressId = this.selectedAddressControl.value!

    this.orderService.createOrder(selectedAddressId, this.shippingFee).subscribe({
      next: (order) => {
        this.snackBar.open("Order placed successfully! Redirecting to payment...", "Close", {
          duration: 3000,
        })
        this.processPayment(order.id)
      },
      error: (err) => {
        console.error("Error placing order:", err)
        this.snackBar.open(`Failed to place order: ${err.error?.message || err.message}`, "Close", {
          duration: 5000,
        })
        this.isPlacingOrder = false
      },
    })
  }

  processPayment(orderId: number): void {
    this.paymentService.createCheckoutSession(orderId).subscribe({
      next: (session) => {
        // Redirect to Stripe Checkout page
        window.location.href = session.url
      },
      error: (err) => {
        console.error("Error creating checkout session:", err)
        this.snackBar.open(`Failed to initiate payment: ${err.error?.message || err.message}`, "Close", {
          duration: 5000,
        })
        this.isPlacingOrder = false
      },
    })
  }
}
