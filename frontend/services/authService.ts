import axiosClient from "@/lib/axiosClient";
import { tokenStorage } from "@/lib/tokenStorage";
import type { LoginRequest, LoginResponse, RegisterRequest, User } from "@/types/auth";

export const authService = {
  register: async (data: RegisterRequest) => {
    const res = await axiosClient.post("/api/auth/register", data);
    return res.data;
  },

  login: async (data: LoginRequest) => {
    const res = await axiosClient.post<LoginResponse>("/api/auth/login", data);

    const token = res.data.token;
    tokenStorage.setToken(token);

    return res.data;
  },

  getCurrentUser: async () => {
    const res = await axiosClient.get<User>("/api/auth/me");
    return res.data;
  },

  logout: () => {
    tokenStorage.removeToken();
    window.location.href = "/login";
  },
};