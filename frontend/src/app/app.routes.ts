import { Routes } from "@angular/router"
import { HomeComponent } from "./features/home/home.component"
import { authGuard } from "./core/guards/auth.guard"
import { adminGuard } from "./core/guards/admin.guard"

export const appRoutes: Routes = [
  { path: "", component: HomeComponent, title: "Home" },
  {
    path: "auth",
    loadChildren: () => import("./features/auth/auth.routes").then((m) => m.AUTH_ROUTES),
  },
  {
    path: "products",
    loadChildren: () => import("./features/products/products.routes").then((m) => m.PRODUCTS_ROUTES),
  },
  {
    path: "cart",
    loadChildren: () => import("./features/cart/cart.routes").then((m) => m.CART_ROUTES),
    canActivate: [authGuard],
  },
  {
    path: "orders",
    loadChildren: () => import("./features/orders/orders.routes").then((m) => m.ORDERS_ROUTES),
    canActivate: [authGuard],
  },
  {
    path: "profile",
    loadChildren: () => import("./features/profile/profile.routes").then((m) => m.PROFILE_ROUTES),
    canActivate: [authGuard],
  },
  {
    path: "admin",
    loadChildren: () => import("./features/admin/admin.routes").then((m) => m.ADMIN_ROUTES),
    canActivate: [authGuard, adminGuard],
  },
  { path: "**", redirectTo: "" }, // Redirect unknown paths to home
]
