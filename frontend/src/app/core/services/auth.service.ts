import { Injectable, inject } from "@angular/core"
import { HttpClient } from "@angular/common/http"
import type { Observable } from "rxjs"
import { environment } from "../../../environments/environment"
import type { LoginRequest, SignupRequest, OtpVerifyRequest, JwtResponse, User } from "../models/user.model"
import {map} from "rxjs/operators";

@Injectable({
  providedIn: "root",
})
export class AuthService {
  private http = inject(HttpClient)
  private apiUrl = `${environment.apiUrl}/api/auth`

  signup(request: SignupRequest): Observable<string> {
    return this.http.post<string>(`${this.apiUrl}/signup`, request)
  }

  verifyEmail(request: OtpVerifyRequest): Observable<string> {
    return this.http.post<string>(`${this.apiUrl}/verify-email`, request)
  }

  // Update login method to handle OTP response
  login(request: LoginRequest): Observable<string> {
    return this.http.post<string>(`${this.apiUrl}/login`, request).pipe(
        map(response => {
          // Check if backend indicates OTP is required
          if (response === 'OTP_REQUIRED') {
            return 'OTP_REQUIRED';
          }
          return response;
        })
    );
  }

  loginVerifyOtp(request: OtpVerifyRequest): Observable<JwtResponse> {
    return this.http.post<JwtResponse>(`${this.apiUrl}/login/verify-otp`, request)
  }

  resendOtp(email: string, type: string): Observable<string> {
    return this.http.post<string>(`${this.apiUrl}/resend-otp`, { email, type })
  }

  forgotPassword(email: string): Observable<string> {
    return this.http.post<string>(`${this.apiUrl}/forgot-password`, { email })
  }

  resetPassword(email: string, otp: string, newPassword: string): Observable<string> {
    return this.http.post<string>(`${this.apiUrl}/reset-password`, {
      email,
      otp,
      newPassword,
    })
  }

  updateUserProfile(user: Partial<User>): Observable<User> {
 return this.http.put<User>(`${environment.apiUrl}/api/users/me`, user);
  }

  changePassword(passwordChangeRequest: any): Observable<any> {
 return this.http.put<any>(`${environment.apiUrl}/api/users/me/password`, passwordChangeRequest);
  }

  getCurrentUser(): Observable<User> {
    return this.http.get<User>(`${environment.apiUrl}/api/users/me`)
  }

  logout(): void {
    localStorage.removeItem("token")
  }

  getToken(): string | null {
    return localStorage.getItem("token")
  }

  setToken(token: string): void {
    localStorage.setItem("token", token)
  }

  isAuthenticated(): boolean {
    const token = this.getToken()
    if (!token) return false

    try {
      const payload = JSON.parse(atob(token.split(".")[1]))
      return payload.exp > Date.now() / 1000
    } catch {
      return false
    }
  }

  // Admin User Management Methods
  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${environment.apiUrl}/api/users`);
  }

  updateUserStatus(id: number, status: boolean): Observable<User> {
    return this.http.put<User>(`${environment.apiUrl}/api/users/${id}/status`, { active: status });
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/api/users/${id}`);
  }

  createUser(user: any): Observable<User> {
    return this.http.post<User>(`${environment.apiUrl}/api/users`, user);
  }
}
