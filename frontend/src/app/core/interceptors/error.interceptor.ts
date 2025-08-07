// src/app/core/interceptors/error.interceptor.ts

import type { HttpInterceptorFn, HttpErrorResponse } from "@angular/common/http"
import { inject } from "@angular/core"
import { Router } from "@angular/router"
import { catchError, throwError } from "rxjs"
import { MatSnackBar } from "@angular/material/snack-bar"

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
    const router = inject(Router)
    const snackBar = inject(MatSnackBar)

    return next(req).pipe(
        catchError((error: HttpErrorResponse) => {
            let errorMessage = "An error occurred"

            if (error.error instanceof ErrorEvent) {
                errorMessage = error.error.message
            } else {
                switch (error.status) {
                    case 401:
                        errorMessage = "Unauthorized access"
                        localStorage.removeItem("token")
                        router.navigate(["/auth/login"])
                        break
                    case 403:
                        errorMessage = "Access forbidden"
                        break
                    case 404:
                        errorMessage = "Resource not found"
                        break
                    case 500:
                        errorMessage = "Internal server error"
                        break
                    default:
                        errorMessage = error.error?.message || `Error: ${error.status}`
                }
            }

            snackBar.open(errorMessage, "Close", {
                duration: 5000,
                horizontalPosition: "end",
                verticalPosition: "top",
            })

            return throwError(() => error)
        }),
    )
}
