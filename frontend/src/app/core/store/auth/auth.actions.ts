// auth.actions.ts
import { createActionGroup, emptyProps, props } from "@ngrx/store"
import type { User, LoginRequest, SignupRequest, OtpVerifyRequest, JwtResponse } from "../../models/user.model"

export const AuthActions = createActionGroup({
  source: "Auth",
  events: {
    Login: props<{ request: LoginRequest }>(),
    "Login Success": props<{ message: string }>(),
    "Login Requires OTP": props<{ email: string }>(),
    "Login Verify OTP": props<{ request: OtpVerifyRequest }>(),
    "Login Complete": props<{ response: JwtResponse; user: User }>(),
    Signup: props<{ request: SignupRequest }>(),
    "Signup Success": props<{ message: string }>(),
    "Verify Email": props<{ request: OtpVerifyRequest }>(),
    "Verify Email Success": props<{ message: string }>(),
    "Check Auth Status": emptyProps(),
    "Load User Success": props<{ user: User }>(),
    Logout: emptyProps(),
    "Auth Error": props<{ error: string }>(),
    "Resend OTP": props<{ email: string; otpType: string }>(),
    "Resend OTP Success": props<{ message: string }>(),
    "Forgot Password": props<{ email: string }>(),
    "Forgot Password Success": props<{ message: string }>(),
    "Reset Password": props<{ email: string; otp: string; newPassword: string }>(),
    "Reset Password Success": props<{ message: string }>(),

    // New action to reset only UI state (loading, error, message, OTP flags)
    "Reset Auth UI": emptyProps(),
  },
})
