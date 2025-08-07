import { Routes } from "@angular/router"
import { ProductListComponent } from "./components/product-list/product-list.component"
import { ProductDetailComponent } from "./components/product-detail/product-detail.component"

export const PRODUCTS_ROUTES: Routes = [
  { path: "", component: ProductListComponent, title: "Products" },
  { path: ":id", component: ProductDetailComponent, title: "Product Details" },
]
