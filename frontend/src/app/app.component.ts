import { Component, inject, OnInit } from "@angular/core"
import { CommonModule } from "@angular/common"
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from "@angular/router"
import { MatToolbarModule } from "@angular/material/toolbar"
import { MatButtonModule } from "@angular/material/button"
import { MatIconModule } from "@angular/material/icon"
import { MatSidenavModule } from "@angular/material/sidenav"
import { MatListModule } from "@angular/material/list"
import { MatMenuModule } from "@angular/material/menu"
import { Store } from "@ngrx/store"
import { Observable } from "rxjs"
import { selectIsAuthenticated, selectCurrentUser } from "./core/store/auth/auth.selectors"
import { AuthActions } from "./core/store/auth/auth.actions"
import { User } from "./core/models/user.model"

@Component({
  selector: "app-root",
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatSidenavModule,
    MatListModule,
    MatMenuModule,
  ],
  template: `
    <mat-toolbar color="primary">
      <button mat-icon-button (click)="sidenav.toggle()">
        <mat-icon>menu</mat-icon>
      </button>
      <span routerLink="/" class="app-title">E-commerce App</span>
      <span class="spacer"></span>

      <ng-container *ngIf="isAuthenticated$ | async; else guestMenu">
        <button mat-button routerLink="/products">Products</button>
        <button mat-button routerLink="/cart">
          <mat-icon>shopping_cart</mat-icon> Cart
        </button>
        <button mat-button [matMenuTriggerFor]="userMenu">
          <mat-icon>account_circle</mat-icon>
          {{ (currentUser$ | async)?.username || 'User' }}
        </button>
        <mat-menu #userMenu="matMenu">
          <button mat-menu-item routerLink="/profile">
            <mat-icon>person</mat-icon> My Profile
          </button>
          <button mat-menu-item routerLink="/orders">
            <mat-icon>receipt</mat-icon> My Orders
          </button>
          <button mat-menu-item *ngIf="(currentUser$ | async)?.role === 'ADMIN'" routerLink="/admin">
            <mat-icon>admin_panel_settings</mat-icon> Admin Dashboard
          </button>
          <button mat-menu-item (click)="logout()">
            <mat-icon>logout</mat-icon> Logout
          </button>
        </mat-menu>
      </ng-container>

      <ng-template #guestMenu>
        <button mat-button routerLink="/products">Products</button>
        <button mat-button routerLink="/auth/login">Login</button>
        <button mat-button routerLink="/auth/signup">Sign Up</button>
      </ng-template>
    </mat-toolbar>

    <mat-sidenav-container class="sidenav-container">
      <mat-sidenav #sidenav mode="side" [opened]="false">
        <mat-nav-list>
          <a mat-list-item routerLink="/" (click)="sidenav.close()">Home</a>
          <a mat-list-item routerLink="/products" (click)="sidenav.close()">Products</a>
          <a mat-list-item routerLink="/cart" (click)="sidenav.close()">Cart</a>
          <ng-container *ngIf="isAuthenticated$ | async">
            <a mat-list-item routerLink="/profile" (click)="sidenav.close()">My Profile</a>
            <a mat-list-item routerLink="/orders" (click)="sidenav.close()">My Orders</a>
            <a mat-list-item *ngIf="(currentUser$ | async)?.role === 'ADMIN'" routerLink="/admin" (click)="sidenav.close()">Admin Dashboard</a>
            <a mat-list-item (click)="logout(); sidenav.close()">Logout</a>
          </ng-container>
          <ng-container *ngIf="!(isAuthenticated$ | async)">
            <a mat-list-item routerLink="/auth/login" (click)="sidenav.close()">Login</a>
            <a mat-list-item routerLink="/auth/signup" (click)="sidenav.close()">Sign Up</a>
          </ng-container>
        </mat-nav-list>
      </mat-sidenav>
      <mat-sidenav-content>
        <main class="content">
          <router-outlet></router-outlet>
        </main>
      </mat-sidenav-content>
    </mat-sidenav-container>
  `,
  styles: [
    `
      .app-title {
        cursor: pointer;
      }
      .spacer {
        flex: 1 1 auto;
      }
      .sidenav-container {
        height: calc(100vh - 64px); /* Adjust based on toolbar height */
      }
      mat-sidenav {
        width: 250px;
      }
      .content {
        padding: 20px;
      }
    `,
  ],
})
export class AppComponent implements OnInit {
  private store = inject(Store)
  private router = inject(Router)

  isAuthenticated$: Observable<boolean> = this.store.select(selectIsAuthenticated)
  currentUser$: Observable<User | null> = this.store.select(selectCurrentUser)

  ngOnInit(): void {
    // Attempt to load user on app start if a token exists
    if (localStorage.getItem("token")) {
      this.store.dispatch(AuthActions.loadUser())
    }
  }

  logout(): void {
    this.store.dispatch(AuthActions.logout())
  }
}
