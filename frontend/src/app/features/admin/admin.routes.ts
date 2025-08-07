import { Routes } from "@angular/router"
import { AdminDashboardComponent } from "./components/admin-dashboard/admin-dashboard.component"
import { AdminUsersComponent } from "./components/admin-users/admin-users.component"
import { AdminProductsComponent } from "./components/admin-products/admin-products.component"
import { AdminCategoriesComponent } from "./components/admin-categories/admin-categories.component"
import { AdminSuppliersComponent } from "./components/admin-suppliers/admin-suppliers.component"
import { AdminActivityLogsComponent } from "./components/admin-activity-logs/admin-activity-logs.component"
import { AdminImportLogsComponent } from "./components/admin-import-logs/admin-import-logs.component"
import { AdminSupplierPaymentsComponent } from "./components/admin-supplier-payments/admin-supplier-payments.component"
import { AdminUserCreateComponent } from "./components/admin-users/admin-user-create/admin-user-create.component"

export const ADMIN_ROUTES: Routes = [
  { path: "", component: AdminDashboardComponent, title: "Admin Dashboard" },
  { path: "users", component: AdminUsersComponent, title: "Manage Users" },
  { path: "users/create", component: AdminUserCreateComponent, title: "Create User" },
  { path: "products", component: AdminProductsComponent, title: "Manage Products" },
  { path: "categories", component: AdminCategoriesComponent, title: "Manage Categories" },
  { path: "suppliers", component: AdminSuppliersComponent, title: "Manage Suppliers" },
  { path: "activity-logs", component: AdminActivityLogsComponent, title: "Activity Logs" },
  { path: "import-logs", component: AdminImportLogsComponent, title: "Import Logs" },
  {
    path: "supplier-payments",
    component: AdminSupplierPaymentsComponent,
    title: "Supplier Payments",
  },
]
