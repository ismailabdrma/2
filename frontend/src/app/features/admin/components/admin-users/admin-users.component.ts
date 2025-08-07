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
import { MatDialog } from "@angular/material/dialog"
import { MatDialog, MatDialogModule } from "@angular/material/dialog"
import { MatSnackBar } from "@angular/material/snack-bar"
import { AuthService } from "@core/services/auth.service"
import { AdminUserCreateComponent } from "./admin-user-create/admin-user-create.component"
import type { User } from "@core/models/user.model"
import { catchError, of } from "rxjs"

import { ConfirmDialogComponent } from "@shared/components/confirm-dialog/confirm-dialog.component"

@Component({
  selector: "app-admin-users",
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
    MatDialogModule,
  ],
  templateUrl: "./admin-users.component.html",
  styleUrl: "./admin-users.component.scss",
})
export class AdminUsersComponent implements OnInit {
  private authService = inject(AuthService)
  private dialog = inject(MatDialog)
  private snackBar = inject(MatSnackBar)

  users: User[] = []
  loading = true
  displayedColumns: string[] = ["username", "email", "role", "status", "actions"]

  ngOnInit(): void {
    this.loadUsers()
  }

  loadUsers(): void {
    this.loading = true
 this.authService.getAllUsers().subscribe({
      next: (users) => { this.users = users; this.loading = false },
      error: (error) => { this.snackBar.open('Error loading users.', 'Close', { duration: 3000 }); this.loading = false }
    });
  }

  openCreateDialog(): void {
    const dialogRef = this.dialog.open(AdminUserCreateComponent, {
      width: "500px",
    })

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.loadUsers() // Refresh users list after creation
      }
    })
  }

  toggleUserStatus(user: User): void {
    const newStatus = !user.isActive
 this.authService.updateUserStatus(user.id!, newStatus).subscribe({
      next: () => { user.isActive = newStatus; this.snackBar.open(`User ${user.username} status updated.`, 'Close', { duration: 3000 }) },
      error: (error) => this.snackBar.open(`Error updating user ${user.username} status.`, 'Close', { duration: 3000 })
    });
  }

  deleteUser(id: number): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Confirm Deletion',
        message: 'Are you sure you want to delete this user?',
      },
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.authService.deleteUser(id).subscribe({
          next: () => { this.snackBar.open('User deleted successfully.', 'Close', { duration: 3000 }); this.loadUsers() },
          error: (error) => this.snackBar.open('Error deleting user.', 'Close', { duration: 3000 })
        });
      }
    });
  }
}
