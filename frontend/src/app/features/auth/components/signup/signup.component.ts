import { Component, inject } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormBuilder, type FormGroup, Validators, ReactiveFormsModule } from "@angular/forms";
import { Router } from "@angular/router";
import { MatCardModule } from "@angular/material/card";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatButtonModule } from "@angular/material/button";
import { MatIconModule } from "@angular/material/icon";
import { MatSelectModule } from "@angular/material/select";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { MatSnackBar } from "@angular/material/snack-bar";
import { Store } from "@ngrx/store";
import type { Observable } from "rxjs";
import { tap } from "rxjs/operators";
import { AuthActions } from "../../../../core/store/auth/auth.actions";
import { selectAuthLoading, selectAuthMessage } from "@core/store/auth/auth.selectors";

@Component({
  selector: "app-signup",
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: "./signup.component.html",
  styleUrl: "./signup.component.scss",
})
export class SignupComponent {
  private fb = inject(FormBuilder);
  private store = inject(Store);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);

  signupForm: FormGroup;
  hidePassword = true;
  hideConfirmPassword = true;
  loading$: Observable<boolean> = this.store.select(selectAuthLoading);
  message$: Observable<string | null> = this.store.select(selectAuthMessage);
  error$: Observable<string | null> = this.store.select(selectAuthError);

  constructor() {
    this.signupForm = this.fb.group(
        {
          username: ["", [Validators.required]],
          email: ["", [Validators.required, Validators.email]],
          password: ["", [Validators.required, Validators.minLength(6)]],
          confirmPassword: ["", [Validators.required]],
          role: ["USER", [Validators.required]],
        },
        { validators: this.passwordMatchValidator },
    );
  }

  ngOnInit() {
 this.error$.subscribe(error => {
 if (error) {
 this.snackBar.open(error, "Close", { duration: 5000 });
 }
 });

    this.message$.pipe(
        tap(message => {
          if (message) {
            this.snackBar.open(message, "Close", { duration: 5000 });
            if (message.includes("successful")) {
              this.router.navigate(["/auth/verify-email"], {
                queryParams: { email: this.signupForm.value.email },
              });
            }
          }
        })
    ).subscribe();
  }

  passwordMatchValidator(form: FormGroup) {
    const password = form.get("password");
    const confirmPassword = form.get("confirmPassword");

    if (password && confirmPassword && password.value !== confirmPassword.value) {
      confirmPassword.setErrors({ mismatch: true });
    } else {
      confirmPassword?.setErrors(null);
    }

    return null;
  }

  onSubmit(): void {
    if (this.signupForm.valid) {
      const { confirmPassword, ...signupData } = this.signupForm.value;
      this.store.dispatch(AuthActions.signup({ request: signupData }));
    }
  }
}
