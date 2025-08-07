import { createFeatureSelector, createSelector } from "@ngrx/store"
import type { AuthState } from "./auth.reducer"

export const selectAuthState = createFeatureSelector<AuthState>("auth")

export const selectUser = createSelector(selectAuthState, (state) => state.user)

export const selectIsAuthenticated = createSelector(selectAuthState, (state) => state.isAuthenticated)

export const selectAuthLoading = createSelector(selectAuthState, (state) => state.loading)

export const selectAuthError = createSelector(selectAuthState, (state) => state.error)

export const selectAuthMessage = createSelector(selectAuthState, (state) => state.message)
export const selectRequiresOtp = createSelector(selectAuthState, (state) => state.requiresOtp)
export const selectOtpEmail = createSelector(selectAuthState, (state) => state.otpEmail)
