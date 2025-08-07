import { Component, inject } from "@angular/core"
import { CommonModule } from "@angular/common"
import { FormBuilder, type FormGroup, Validators, ReactiveFormsModule } from "@angular/forms"
import { MatDialogRef, MatDialogModule } from "@angular/material/dialog"
import { MatFormFieldModule } from "@angular/material/form-field"
import { MatInputModule } from "@angular/material/input"
import { MatSelectModule } from "@angular/material/select"
import { MatButtonModule } from "@angular/material/button"
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner"
import { AuthService } from "@core/services/auth.service"
import { MatSnackBar } from "@angular/material/snack-bar"

@Component({
  selector: "app-admin-user-create",
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatProgressSpinnerModule,
  ],
  template: `
    <h2 mat-dialog-title>Create User</h2>

    <mat-dialog-content>
      <form [formGroup]="userForm">
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Username</mat-label>
          <input matInput formControlName="username" required>
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Email</mat-label>
          <input matInput type="email" formControlName="email" required>
        </mat-form-field>

        <div class="form-row">
          <mat-form-field appearance="outline" class="half-width">
            <mat-label>First Name</mat-label>
            <input matInput formControlName="firstName">
          </mat-form-field>

          <mat-form-field appearance="outline" class="half-width">
            <mat-label>Last Name</mat-label>
            <input matInput formControlName="lastName">
          </mat-form-field>
        </div>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Phone</mat-label>
          <input matInput formControlName="phone">
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Role</mat-label>
          <mat-select formControlName="role" required>
            <mat-option value="CLIENT">Client</mat-option>
            <mat-option value="ADMIN">Admin</mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Password</mat-label>
          <input matInput type="password" formControlName="password" required>
        </mat-form-field>
      </form>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Cancel</button>
      <button mat-raised-button color="primary" 
              (click)="onSave()" 
              [disabled]="userForm.invalid || loading">
        <mat-spinner diameter="20" *ngIf="loading"></mat-spinner>
        <span *ngIf="!loading">Create User</span>
      </button>
    </mat-dialog-actions>
  `,
  styles: [
    `
    .full-width {
      width: 100%;
      margin-bottom: 16px;
    }

    .form-row {
      display: flex;
      gap: 16px;
    }

    .half-width {
      flex: 1;
      margin-bottom: 16px;
    }

    mat-dialog-content {
      min-width: 400px;
    }

    mat-spinner {
      margin-right: 8px;
    }

    @media (max-width: 768px) {
      .form-row {
        flex-direction: column;
      }
      
      mat-dialog-content {
        min-width: 300px;
      }
    }
  `,
  ],
})
export class AdminUserCreateComponent {
  private fb = inject(FormBuilder)
  private authService = inject(AuthService)
  private dialogRef = inject(MatDialogRef<AdminUserCreateComponent>)
 private snackBar = inject(MatSnackBar)
 
  userForm: FormGroup
  loading = false

  constructor() {
    this.userForm = this.fb.group({
      username: ["", Validators.required],
      email: ["", [Validators.required, Validators.email]],
      firstName: [""],
      lastName: [""],
      phone: [""],
      role: ["CLIENT", Validators.required],
      password: ["", [Validators.required, Validators.minLength(6)]],
    })
  }

  onSave(): void {
    if (this.userForm.valid) {
      this.loading = true
      this.authService.createUser(this.userForm.value).subscribe({
        next: (newUser) => {
          this.loading = false;
          this.snackBar.open('User created successfully!', 'Close', { duration: 3000 });
          this.dialogRef.close(newUser); // Close dialog and pass the new user data
        },
        error: (error) => {
          console.error('Error creating user:', error);
          this.loading = false;
          const errorMessage = error.error?.message || 'Failed to create user.';
          this.snackBar.open(errorMessage, 'Close', { duration: 3000 });
        }
      });
    }
  }

  onCancel(): void {
    this.dialogRef.close()
  }
}
