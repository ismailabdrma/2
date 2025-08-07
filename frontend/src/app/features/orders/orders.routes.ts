import { Routes } from "@angular/router"
import { OrderListComponent } from "./components/order-list/order-list.component"
import { OrderDetailComponent } from "./components/order-detail/order-detail.component"
import { CheckoutComponent } from "./components/checkout/checkout.component"

export const ORDERS_ROUTES: Routes = [
  { path: "", component: OrderListComponent, title: "My Orders" },
  { path: "checkout", component: CheckoutComponent, title: "Checkout" },
  { path: ":id", component: OrderDetailComponent, title: "Order Details" },
]
