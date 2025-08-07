import { CanActivateFn, Router } from "@angular/router"
import { inject } from "@angular/core"
import { AuthService } from "../services/auth.service"
import { MatSnackBar } from "@angular/material/snack-bar"

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService)
  const router = inject(Router)
  const snackBar = inject(MatSnackBar)

  if (authService.isAuthenticated()) {
    return true
  } else {
    snackBar.open("You need to be logged in to access this page.", "Close", { duration: 3000 })
    router.navigate(["/auth/login"])
    return false
  }
}
