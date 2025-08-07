import {Component, inject, OnInit} from "@angular/core"
import { CommonModule } from "@angular/common"
import { FormBuilder, type FormGroup, Validators, ReactiveFormsModule } from "@angular/forms"
import { RouterModule } from "@angular/router"
import { MatCardModule } from "@angular/material/card"
import { MatFormFieldModule } from "@angular/material/form-field"
import { MatInputModule } from "@angular/material/input"
import { MatButtonModule } from "@angular/material/button"
import { MatIconModule } from "@angular/material/icon"
import { MatSnackBar, MatSnackBarModule } from "@angular/material/snack-bar"
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner"
import { Store } from "@ngrx/store"
import type { Observable, Subscription } from "rxjs"
import { AuthActions } from "@core/store/auth/auth.actions"
import {selectAuthError, selectAuthLoading, selectAuthMessage} from "@core/store/auth/auth.selectors"

@Component({
  selector: "app-reset-password",
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: "./reset-password.component.html",
  styleUrl: "./reset-password.component.scss",
})
export class ResetPasswordComponent implements OnInit {
  private fb = inject(FormBuilder)
  private store = inject(Store)

  resetPasswordForm: FormGroup
  hidePassword = true
  hideConfirmPassword = true
  loading$: Observable<boolean> = this.store.select(selectAuthLoading)
  error$: Observable<string | null> = this.store.select(selectAuthError);
  message$: Observable<string | null> = this.store.select(selectAuthMessage);

  constructor() {
    this.resetPasswordForm = this.fb.group(
      {
        email: ["", [Validators.required, Validators.email]],
        otp: ["", [Validators.required, Validators.pattern(/^\d{6}$/)]],
        newPassword: ["", [Validators.required, Validators.minLength(6)]],
        confirmPassword: ["", [Validators.required]],
      },
      { validators: this.passwordMatchValidator },
    )
  }

  ngOnInit(): void {
    this.message$.subscribe(message => {
      if (message) {
        this.snackBar.open(message, "Close", { duration: 5000 });
        // TODO: Implement intelligent redirection after successful password reset
      }
    });

    this.error$.subscribe(error => {
      if (error) {
        this.snackBar.open(error, "Close", { duration: 5000 });
      }
    });
  }


  passwordMatchValidator(form: FormGroup) {
    const newPassword = form.get("newPassword")
    const confirmPassword = form.get("confirmPassword")

    if (newPassword && confirmPassword && newPassword.value !== confirmPassword.value) {
      confirmPassword.setErrors({ mismatch: true })
    } else {
      confirmPassword?.setErrors(null)
    }

    return null
  }

  onSubmit(): void {
    if (this.resetPasswordForm.valid) {
      const { email, otp, newPassword } = this.resetPasswordForm.value
      this.store.dispatch(AuthActions.resetPassword({ email, otp, newPassword }))
      // TODO: Implement intelligent redirection to login page after successful password reset, likely handled in NgRx effects.
    }
  }
}
