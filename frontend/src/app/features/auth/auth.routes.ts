import { Routes } from "@angular/router"
import { LoginComponent } from "./components/login/login.component"
import { SignupComponent } from "./components/signup/signup.component"
import { VerifyEmailComponent } from "./components/verify-email/verify-email.component"
import { VerifyOtpComponent } from "./components/verify-otp/verify-otp.component"
import { ForgotPasswordComponent } from "./components/forgot-password/forgot-password.component"
import { ResetPasswordComponent } from "./components/reset-password/reset-password.component"

export const AUTH_ROUTES: Routes = [
  { path: "login", component: LoginComponent, title: "Login" },
  { path: "signup", component: SignupComponent, title: "Sign Up" },
  { path: "verify-email", component: VerifyEmailComponent, title: "Verify Email" },
  { path: "verify-otp", component: VerifyOtpComponent, title: "Verify OTP" },
  { path: "forgot-password", component: ForgotPasswordComponent, title: "Forgot Password" },
  { path: "reset-password", component: ResetPasswordComponent, title: "Reset Password" },
  { path: "", redirectTo: "login", pathMatch: "full" },
]
