import { Component, OnInit, inject } from "@angular/core"
import { CommonModule } from "@angular/common"
import { ReactiveFormsModule, FormBuilder, Validators, AbstractControl, ValidatorFn } from "@angular/forms"
import { MatInputModule } from "@angular/material/input"
import { MatFormFieldModule } from "@angular/material/form-field"
import { MatButtonModule } from "@angular/material/button"
import { MatCardModule } from "@angular/material/card"
import { MatDividerModule } from "@angular/material/divider"
import { RouterLink } from "@angular/router"
import { Store } from "@ngrx/store"
import { AuthActions } from "../../../../core/store/auth/auth.actions"
import { selectCurrentUser } from "../../../../core/store/auth/auth.selectors"
import { User } from "../../../../core/models/user.model"
import { MatSnackBar } from "@angular/material/snack-bar"

// Custom validator for password mismatch
export function passwordMatchValidator(
  passwordControlName: string,
  confirmPasswordControlName: string,
): ValidatorFn {
  return (control: AbstractControl): { [key: string]: any } | null => {
    const password = control.get(passwordControlName)
    const confirmPassword = control.get(confirmPasswordControlName)

    if (!password || !confirmPassword) {
      return null // Controls not found
    }

    if (password.value !== confirmPassword.value) {
      confirmPassword.setErrors({ passwordMismatch: true })
      return { passwordMismatch: true }
    } else {
      confirmPassword.setErrors(null) // Clear error if they match
      return null
    }
  }
}

@Component({
  selector: "app-profile",
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatInputModule,
    MatFormFieldModule,
    MatButtonModule,
    MatCardModule,
    MatDividerModule,
    RouterLink,
  ],
  templateUrl: "./profile.component.html",
  styleUrl: "./profile.component.scss",
})
export class ProfileComponent implements OnInit {
  private fb = inject(FormBuilder)
  private store = inject(Store)
  private snackBar = inject(MatSnackBar)

  currentUser: User | null = null

  profileForm = this.fb.group({
    username: ["", Validators.required],
    email: ["", [Validators.required, Validators.email]],
    firstName: [""],
    lastName: [""],
    phone: [""],
  })

  passwordForm = this.fb.group(
    {
      currentPassword: ["", Validators.required],
      newPassword: ["", [Validators.required, Validators.minLength(6)]],
      confirmPassword: ["", Validators.required],
    },
    { validators: passwordMatchValidator("newPassword", "confirmPassword") },
  )

  ngOnInit(): void {
    this.store.select(selectCurrentUser).subscribe((user) => {
      this.currentUser = user
      if (user) {
        this.profileForm.patchValue({
          username: user.username,
          email: user.email,
          firstName: user.firstName || "",
          lastName: user.lastName || "",
          phone: user.phone || "",
        })
      }
    })
  }

  updateProfile(): void {
    if (this.profileForm.valid && this.currentUser) {
      const updatedUser: Partial<User> = {
        id: this.currentUser.id,
        username: this.profileForm.value.username!,
        email: this.profileForm.value.email!,
        firstName: this.profileForm.value.firstName || undefined,
        lastName: this.profileForm.value.lastName || undefined,
        phone: this.profileForm.value.phone || undefined,
      }
      this.store.dispatch(AuthActions.updateUserProfile({ user: updatedUser }))
    }
  }

  changePassword(): void {
    if (this.passwordForm.valid) {
      const { currentPassword, newPassword } = this.passwordForm.value
      this.store.dispatch(
        AuthActions.changePassword({
          passwordChangeRequest: { currentPassword, newPassword },
        }),
      )
      this.passwordForm.reset() // Clear form after submission
    } else {
      this.snackBar.open("Please correct the password fields.", "Close", { duration: 3000 })
    }
  }
}
