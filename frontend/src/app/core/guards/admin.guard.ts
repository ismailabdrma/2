import { CanActivateFn, Router } from "@angular/router"
import { inject } from "@angular/core"
import { Store } from "@ngrx/store"
import { selectCurrentUser } from "../store/auth/auth.selectors"
import { map, take } from "rxjs/operators"

export const adminGuard: CanActivateFn = (route, state) => {
  const store = inject(Store)
  const router = inject(Router)

  return store.select(selectCurrentUser).pipe(
    take(1), // Take the current value and complete
    map((user) => {
      if (user && user.role === "ADMIN") {
        return true
      } else {
        // Redirect to home or an unauthorized page
        router.navigate(["/"])
        return false
      }
    }),
  )
}
