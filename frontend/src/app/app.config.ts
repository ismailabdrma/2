import { ApplicationConfig, provideZoneChangeDetection, isDevMode } from "@angular/core"
import { provideRouter } from "@angular/router"
import { provideAnimations } from "@angular/platform-browser/animations"
import { provideHttpClient, withInterceptors } from "@angular/common/http"
import { provideStore } from "@ngrx/store"
import { provideEffects } from "@ngrx/effects"
import { provideStoreDevtools } from "@ngrx/store-devtools"

import { routes } from "./app.routes"
import { authReducer } from "./core/store/auth/auth.reducer"
import { AuthEffects } from "./core/store/auth/auth.effects"
import { AuthInterceptor } from "./core/interceptors/auth.interceptor"
import { ErrorInterceptor } from "./core/interceptors/error.interceptor"

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideAnimations(),
    provideHttpClient(withInterceptors([AuthInterceptor, ErrorInterceptor])),
    provideStore({ auth: authReducer }),
    provideEffects([AuthEffects]),
    provideStoreDevtools({ maxAge: 25, logOnly: !isDevMode() }),
  ],
}
