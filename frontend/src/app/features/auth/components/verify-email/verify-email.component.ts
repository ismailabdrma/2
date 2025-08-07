import { Component, inject, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormBuilder, type FormGroup, Validators, ReactiveFormsModule } from "@angular/forms";
import { Router, ActivatedRoute } from "@angular/router";
import { MatCardModule } from "@angular/material/card";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatButtonModule } from "@angular/material/button";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { MatSnackBar } from "@angular/material/snack-bar";
import { Store } from "@ngrx/store";
import type { Observable } from "rxjs";
import { tap } from "rxjs/operators";
import { AuthActions } from "../../../../core/store/auth/auth.actions";
import {selectAuthError, selectAuthLoading, selectAuthMessage} from "@core/store/auth/auth.selectors";

@Component({
  selector: "app-verify-email",
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: "./verify-email.component.html",
  styleUrl: "./verify-email.component.scss",
})
export class VerifyEmailComponent implements OnInit {
  private fb = inject(FormBuilder);
  private store = inject(Store);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private snackBar = inject(MatSnackBar);

  verifyForm: FormGroup;
  loading$: Observable<boolean> = this.store.select(selectAuthLoading);
  message$: Observable<string | null> = this.store.select(selectAuthMessage);
  error$: Observable<string | null> = this.store.select(selectAuthError);

  constructor() {
    this.verifyForm = this.fb.group({
      email: ["", [Validators.required, Validators.email]],
      otp: ["", [Validators.required, Validators.pattern(/^\d{6}$/)]],
    });
  }

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      if (params["email"]) {
        this.verifyForm.patchValue({ email: params["email"] });
      }
    });

    this.message$.pipe(
        tap(message => {
          if (message) {
            this.snackBar.open(message, "Close", { duration: 5000 });
            if (message.includes("successful")) {
              // Navigate to login with email prefilled
              setTimeout(() => {
                this.router.navigate(["/auth/login"], {
                  queryParams: { email: this.verifyForm.value.email },
                });
              }, 2000);
            }
          }
        })
    ).subscribe();

    this.error$.subscribe(error => {
      if (error) {
        this.snackBar.open(error, "Close", { duration: 5000 });
      }
    });
  }

  onSubmit(): void {
    if (this.verifyForm.valid) {
      this.store.dispatch(AuthActions.verifyEmail({ request: this.verifyForm.value }));
    }
  }

  resendCode(): void {
    const email = this.verifyForm.get("email")?.value;
    if (email) {
      this.store.dispatch(AuthActions.resendOTP({ email, otpType: "EMAIL_VERIFICATION" }));
    }
  }
}
