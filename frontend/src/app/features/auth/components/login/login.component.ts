import { Component, inject, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormBuilder, type FormGroup, Validators, ReactiveFormsModule } from "@angular/forms";
import {Router, ActivatedRoute, RouterLink} from "@angular/router";
import { MatCardModule } from "@angular/material/card";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatButtonModule } from "@angular/material/button";
import { MatIconModule } from "@angular/material/icon";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { MatSnackBar, MatSnackBarModule } from "@angular/material/snack-bar";
import { Store } from "@ngrx/store";
import type { Observable } from "rxjs";
import { AuthActions } from "@core/store/auth/auth.actions";
import { selectAuthLoading, selectAuthError, selectAuthMessage } from "@core/store/auth/auth.selectors";

@Component({
  selector: "app-login",
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    RouterLink,
  ],
  template: `
    <div class="auth-container">
      <mat-card class="auth-card">
        <mat-card-header>
          <mat-card-title>Login</mat-card-title>
          <mat-card-subtitle>Sign in to your account</mat-card-subtitle>
        </mat-card-header>
        <mat-card-content>
          <form [formGroup]="loginForm" (ngSubmit)="onSubmit()">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Username or Email</mat-label>
              <input matInput formControlName="identifier" required />
              <mat-error *ngIf="loginForm.get('identifier')?.hasError('required')">
                Username or email is required
              </mat-error>
            </mat-form-field>
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Password</mat-label>
              <input
                matInput
                [type]="hidePassword ? 'password' : 'text'"
                formControlName="password"
                required
              />
              <button
                mat-icon-button
                matSuffix
                (click)="hidePassword = !hidePassword"
                type="button"
              >
                <mat-icon>{{ hidePassword ? 'visibility_off' : 'visibility' }}</mat-icon>
              </button>
              <mat-error *ngIf="loginForm.get('password')?.hasError('required')">
                Password is required
              </mat-error>
            </mat-form-field>
            <button
              mat-raised-button
              color="primary"
              type="submit"
              class="full-width"
              [disabled]="loginForm.invalid || (loading$ | async)"
            >
              <mat-spinner diameter="20" *ngIf="loading$ | async"></mat-spinner>
              <span *ngIf="!(loading$ | async)">Login</span>
            </button>
          </form>
        </mat-card-content>
        <mat-card-actions align="end">
          <a mat-button routerLink="/auth/forgot-password">Forgot Password?</a>
          <a mat-button routerLink="/auth/signup">Create Account</a>
        </mat-card-actions>
      </mat-card>
    </div>
  `,
  styles: [/* ...same styles... */],
})
export class LoginComponent implements OnInit {
  private fb = inject(FormBuilder);
  private store = inject(Store);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private snackBar = inject(MatSnackBar);

  loginForm: FormGroup;
  hidePassword = true;

  loading$: Observable<boolean> = this.store.select(selectAuthLoading);
  error$: Observable<string | null> = this.store.select(selectAuthError);
  message$: Observable<string | null> = this.store.select(selectAuthMessage);

  constructor() {
    this.loginForm = this.fb.group({
      identifier: ["", [Validators.required]],
      password: ["", [Validators.required]],
    });
  }

  ngOnInit(): void {
    this.store.dispatch(AuthActions.resetAuthUI());
    this.route.queryParams.subscribe(params => {
      if (params["email"]) {
        this.loginForm.patchValue({ identifier: params["email"] });
      }
    });

    this.message$.subscribe(message => {
      if (message) {
        this.snackBar.open(message, "Close", { duration: 5000 });
      }
    });

    this.error$.subscribe(error => {
      if (error) {
        this.snackBar.open(error, "Close", { duration: 5000 });
      }
    });
  }

  onSubmit(): void {
    if (this.loginForm.valid) {
      const formValue = this.loginForm.value;
      this.store.dispatch(AuthActions.login({ request: formValue }));
      // TODO: Intelligent redirection after successful login or requiring OTP is handled in NgRx effects
    }
  }
}
