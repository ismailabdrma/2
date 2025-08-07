import { Component, type OnInit } from "@angular/core"
import { CommonModule } from "@angular/common"
import { RouterModule } from "@angular/router"
import { MatCardModule } from "@angular/material/card"
import { MatButtonModule } from "@angular/material/button"
import { MatIconModule } from "@angular/material/icon"
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner"
import { MatGridListModule } from "@angular/material/grid-list"
@Component({
  selector: "app-admin-dashboard",
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatGridListModule,
  ],
  template: `
    <div class="admin-dashboard-container">
      <h2>Admin Dashboard</h2>

      <div class="stats-grid">
        <mat-card class="stat-card">
          <mat-card-content>
            <div class="stat-content">
              <div class="stat-icon">
                <mat-icon>inventory</mat-icon>
              </div>
              <div class="stat-info">
                <h3>{{ stats.totalProducts }}</h3>
                <p>Total Products</p>
              </div>
            </div>
          </mat-card-content>
        </mat-card>
        <mat-card class="stat-card">
          <mat-card-content>
            <div class="stat-content">
              <div class="stat-icon">
                <mat-icon>local_shipping</mat-icon>
              </div>
              <div class="stat-info">
                <h3>{{ stats.totalSuppliers }}</h3>
                <p>Total Suppliers</p>
              </div>
            </div>
          </mat-card-content>
        </mat-card>
        <mat-card class="stat-card">
          <mat-card-content>
            <div class="stat-content">
              <div class="stat-icon">
                <mat-icon>people</mat-icon>
              </div>
              <div class="stat-info">
                <h3>{{ stats.totalUsers }}</h3>
                <p>Total Users</p>
              </div>
            </div>
          </mat-card-content>
        </mat-card>
        <mat-card class="stat-card">
          <mat-card-content>
            <div class="stat-content">
              <div class="stat-icon">
                <mat-icon>receipt</mat-icon>
              </div>
              <div class="stat-info">
                <h3>{{ stats.totalOrders }}</h3>
                <p>Total Orders</p>
              </div>
            </div>
          </mat-card-content>
        </mat-card>
        <mat-card class="stat-card">
          <mat-card-content>
            <div class="stat-content">
              <div class="stat-icon">
                <mat-icon>attach_money</mat-icon>
              </div>
              <div class="stat-info">
                <h3>\${{ stats.totalRevenue | number:'1.2-2' }}</h3>
                <p>Total Revenue</p>
              </div>
            </div>
          </mat-card-content>
        </mat-card>
      </div>

      <div class="quick-actions">
        <h3>Quick Actions</h3>
        <div class="actions-grid">
          <mat-card class="action-card" routerLink="/admin/products">
            <mat-card-content>
              <mat-icon>inventory</mat-icon>
              <h4>Manage Products</h4>
              <p>Add, edit, or remove products</p>
            </mat-card-content>
          </mat-card>

          <mat-card class="action-card" routerLink="/admin/suppliers">
            <mat-card-content>
              <mat-icon>local_shipping</mat-icon>
              <h4>Manage Suppliers</h4>
              <p>Add, edit, or remove suppliers</p>
            </mat-card-content>
          </mat-card>

          <mat-card class="action-card" routerLink="/admin/orders">
            <mat-card-content>
              <mat-icon>receipt</mat-icon>
              <h4>Manage Orders</h4>
              <p>View and process orders</p>
            </mat-card-content>
          </mat-card>

          <mat-card class="action-card" routerLink="/admin/users">
            <mat-card-content>
              <mat-icon>people</mat-icon>
              <h4>Manage Users</h4>
              <p>View and manage user accounts</p>
            </mat-card-content>
          </mat-card>
        </div>
      </div>
    </div>
  `,
  styles: [/* ...same as before... */],
})
export class AdminDashboardComponent implements OnInit {
  stats = {
    totalProducts: 0,
    totalSuppliers: 0,
    totalUsers: 0,
    totalOrders: 0,
    totalRevenue: 0,
  }

  ngOnInit(): void {
    this.loadStats()
  }

  loadStats(): void {
    // Mock data - replace with your service call as needed
    setTimeout(() => {
      this.stats = {
        totalProducts: 156,
        totalSuppliers: 5,
        totalUsers: 1234,
        totalOrders: 567,
        totalRevenue: 45678.9,
      }
    }, 1000)
  }
}
