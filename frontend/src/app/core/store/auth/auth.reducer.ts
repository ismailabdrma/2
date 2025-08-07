// auth.reducer.ts
import { createReducer, on } from "@ngrx/store"
import type { User } from "../../models/user.model"
import { AuthActions } from "./auth.actions"

export interface AuthState {
    user: User | null
    isAuthenticated: boolean
    loading: boolean
    error: string | null
    message: string | null
    requiresOtp: boolean
    otpEmail: string | null
}

const initialState: AuthState = {
    user: null,
    isAuthenticated: false,
    loading: false,
    error: null,
    message: null,
    requiresOtp: false,
    otpEmail: null,
}

export const authReducer = createReducer(
    initialState,

    on(
        AuthActions.login,
        AuthActions.signup,
        AuthActions.verifyEmail,
        AuthActions.loginVerifyOTP,
        AuthActions.checkAuthStatus,
        AuthActions.resendOTP,
        AuthActions.forgotPassword,
        AuthActions.resetPassword,
        (state) => ({
            ...state,
            loading: true,
            error: null,
            message: null,
        }),
    ),

    on(AuthActions.loginRequiresOTP, (state, { email }) => ({
        ...state,
        requiresOtp: true,
        otpEmail: email,
        loading: false,
        error: null,
    })),

    on(AuthActions.loginComplete, (state, { response, user }) => ({
        ...state,
        user,
        isAuthenticated: true,
        loading: false,
        requiresOtp: false,
        error: null,
    })),

    on(AuthActions.loadUserSuccess, (state, { user }) => ({
        ...state,
        user,
        isAuthenticated: true,
        loading: false,
    })),

    on(
        AuthActions.loginSuccess,
        AuthActions.signupSuccess,
        AuthActions.verifyEmailSuccess,
        AuthActions.resendOTPSuccess,
        AuthActions.forgotPasswordSuccess,
        AuthActions.resetPasswordSuccess,
        (state, { message }) => ({
            ...state,
            loading: false,
            message,
            error: null,
        }),
    ),

    on(AuthActions.logout, () => ({
        ...initialState,
    })),

    on(AuthActions.authError, (state, { error }) => ({
        ...state,
        loading: false,
        error,
        message: null,
        requiresOtp: false,
    })),

    // Handle reset UI state action without logging out the user
    on(AuthActions.resetAuthUI, (state) => ({
        ...state,
        loading: false,
        error: null,
        message: null,
        requiresOtp: false,
        otpEmail: null,
    })),
)
