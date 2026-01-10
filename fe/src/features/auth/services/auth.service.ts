import { apiClient } from "@/lib/api-client";
import { API_CONFIG } from "@/config/api";

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  username: string;
}

export async function login(credentials: LoginRequest): Promise<LoginResponse> {
    console.log("API Config:", API_CONFIG);
    console.log("Login Endpoint:", API_CONFIG.ENDPOINTS.AUTH.LOGIN);
  return apiClient<LoginResponse>(API_CONFIG.ENDPOINTS.AUTH.LOGIN, {
    method: "POST",
    body: JSON.stringify(credentials),
  });
}

export async function logout(token: string): Promise<void> {
  return apiClient<void>(API_CONFIG.ENDPOINTS.AUTH.LOGOUT, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
}
