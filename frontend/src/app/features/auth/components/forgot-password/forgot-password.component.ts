import {Component, inject, OnInit} from "@angular/core"
import { CommonModule } from "@angular/common"
import { FormBuilder, type FormGroup, Validators, ReactiveFormsModule } from "@angular/forms"
import { RouterModule } from "@angular/router"
import { MatCardModule } from "@angular/material/card"
import { MatFormFieldModule } from "@angular/material/form-field"
import { MatInputModule } from "@angular/material/input"
import { MatButtonModule } from "@angular/material/button"
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner"
import { Store } from "@ngrx/store"
import { Observable } from "rxjs"
import { AuthActions } from "@core/store/auth/auth.actions"
import { selectAuthLoading, selectAuthError, selectAuthMessage } from "@core/store/auth/auth.selectors"
import { MatSnackBar, MatSnackBarModule } from "@angular/material/snack-bar";

@Component({
  selector: "app-forgot-password",
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: "./forgot-password.component.html",
  styleUrl: "./forgot-password.component.scss",
})
export class ForgotPasswordComponent implements OnInit {
  private fb = inject(FormBuilder)
  private store = inject(Store)
  private snackBar = inject(MatSnackBar);

  forgotPasswordForm: FormGroup
  loading$: Observable<boolean> = this.store.select(selectAuthLoading)

  constructor() {
    this.forgotPasswordForm = this.fb.group({
      email: ["", [Validators.required, Validators.email]],
    })
  }

  ngOnInit(): void {
    this.store.select(selectAuthError).subscribe(error => {
      if (error) {
        this.snackBar.open(error, 'Close', { duration: 5000 });
      }
    });

    this.store.select(selectAuthMessage).subscribe(message => {
      if (message) {
        this.snackBar.open(message, 'Close', { duration: 5000 });
      }
    });
  }

  onSubmit(): void {
    if (this.forgotPasswordForm.valid) {
      this.store.dispatch(AuthActions.forgotPassword({ email: this.forgotPasswordForm.value.email }))
    }
  }
}
