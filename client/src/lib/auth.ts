import { apiRequest } from "./queryClient";
import type { UserSession } from "@shared/schema";

export interface LoginCredentials {
  email: string;
  password: string;
}

export interface RegisterCredentials {
  username: string;
  email: string;
  password: string;
}

export const authApi = {
  async login(credentials: LoginCredentials): Promise<UserSession> {
    const response = await apiRequest("POST", "/api/auth/login", credentials);
    return response.json();
  },

  async register(credentials: RegisterCredentials): Promise<UserSession> {
    const response = await apiRequest("POST", "/api/auth/register", credentials);
    return response.json();
  },

  async logout(): Promise<void> {
    await apiRequest("POST", "/api/auth/logout");
  },

  async getCurrentUser(): Promise<UserSession> {
    const response = await apiRequest("GET", "/api/auth/me");
    return response.json();
  },
};
