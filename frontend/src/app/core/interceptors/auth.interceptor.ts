// src/app/core/interceptors/auth.interceptor.ts
import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
    const authService = inject(AuthService);
    const token = authService.getToken();

    // Endpoints that should NOT receive the token
    const noAuthEndpoints = [
        '/api/auth/login',
        '/api/auth/signup',
        '/api/auth/verify-email',
        '/api/auth/login/verify-otp',
        '/api/auth/resend-otp',
        '/api/auth/forgot-password',
        '/api/auth/reset-password',
        '/api/products', // Publicly accessible product list
        '/api/products/featured', // Publicly accessible featured products
        /\/api\/products\/\d+/, // Publicly accessible product detail by ID
        /\/api\/products\/\d+\/suggested/, // Publicly accessible suggested products
        '/api/payments/stripe-webhook', // Stripe webhook endpoint
    ];

    // Check if the request URL ends with one of the above endpoints
    const shouldSkipAuth = noAuthEndpoints.some((endpoint) => {
        if (typeof endpoint === 'string') {
            return req.url.endsWith(endpoint);
        } else { // Regex
            return endpoint.test(req.url);
        }
    });

    if (token && !shouldSkipAuth) {
        return next(
            req.clone({
                setHeaders: {
                    Authorization: `Bearer ${token}`,
                },
                withCredentials: true,
            })
        );
    }

    return next(req);
};
