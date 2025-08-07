export interface User {
  id: number
  username: string
  email: string
  firstName?: string
  lastName?: string
  phone?: string
  role: "ADMIN" | "USER"
  active: boolean
}

export interface LoginRequest {
  identifier: string
  password: string
}

export interface SignupRequest {
  username: string
  email: string
  password: string
  role: "ADMIN" | "USER"
}

export interface OtpVerifyRequest {
  email: string
  otp: string
}

export interface JwtResponse {
  token: string
  username: string
  email: string
  role: string
}

export class Address {
}