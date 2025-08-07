import { Component, OnInit, inject } from "@angular/core"
import { CommonModule } from "@angular/common"
import { ReactiveFormsModule, FormBuilder, Validators } from "@angular/forms"
import { MatInputModule } from "@angular/material/input"
import { MatFormFieldModule } from "@angular/material/form-field"
import { MatButtonModule } from "@angular/material/button"
import { MatCardModule } from "@angular/material/card"
import { ActivatedRoute, RouterLink } from "@angular/router"
import { Store } from "@ngrx/store"
import { AuthActions } from "../../../../core/store/auth/auth.actions"
import { OtpVerifyRequest } from "../../../../core/models/user.model"

@Component({
  selector: "app-verify-otp",
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatInputModule,
    MatFormFieldModule,
    MatButtonModule,
    MatCardModule,
    RouterLink,
  ],
  templateUrl: "./verify-otp.component.html",
  styleUrl: "./verify-otp.component.scss",
})
export class VerifyOtpComponent implements OnInit {
  private fb = inject(FormBuilder)
  private store = inject(Store)
  private route = inject(ActivatedRoute)

  verifyOtpForm = this.fb.group({
    email: ["", [Validators.required, Validators.email]],
    otp: ["", Validators.required],
  })

  ngOnInit(): void {
    // Pre-fill email if available from query params (e.g., after login requiring OTP)
    this.route.queryParams.subscribe((params) => {
      if (params["email"]) {
        this.verifyOtpForm.get("email")?.setValue(params["email"])
      }
    })
  }

  onSubmit(): void {
    if (this.verifyOtpForm.valid) {
      const request: OtpVerifyRequest = {
        email: this.verifyOtpForm.value.email!,
        otp: this.verifyOtpForm.value.otp!,
      }
      this.store.dispatch(AuthActions.loginVerifyOtp({ request }))
    }
  }

  resendOtp(): void {
    if (this.verifyOtpForm.get("email")?.valid) {
      const email = this.verifyOtpForm.value.email!
      this.store.dispatch(AuthActions.resendOtp({ email, type: "LOGIN" }))
    }
  }
}
