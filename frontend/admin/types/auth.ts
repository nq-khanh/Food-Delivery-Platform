export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message: string;
  error?: ApiError;
  timestamp: string;
}

export interface ApiError {
  error: string;
  message: string;
  details?: FieldErrorDetail[];
  path: string;
}

export interface FieldErrorDetail {
  field: string;
  message: string;
}

export interface LoginRequest {
  usernameOrEmail: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
}