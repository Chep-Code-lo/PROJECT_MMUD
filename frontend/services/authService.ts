import axiosClient from "@/lib/axiosClient";
import { tokenStorage } from "@/lib/tokenStorage";
import type {
  AuthResponse,
  ForgotPasswordRequest,
  ForgotPasswordResponse,
  LoginRequest,
  MessageResponse,
  RegisterRequest,
  ResetPasswordRequest,
  UserProfile,
} from "@/types/auth";

export const authService = {
  register: async (data: RegisterRequest) => {
    const res = await axiosClient.post<AuthResponse>("/api/auth/register", data);
    tokenStorage.setSession({
      accessToken: res.data.accessToken,
      refreshToken: res.data.refreshToken,
      user: res.data.user,
    });
    return res.data;
  },

  login: async (data: LoginRequest) => {
    const res = await axiosClient.post<AuthResponse>("/api/auth/login", data);
    tokenStorage.setSession({
      accessToken: res.data.accessToken,
      refreshToken: res.data.refreshToken,
      user: res.data.user,
    });
    return res.data;
  },

  getCurrentUser: async () => {
    const res = await axiosClient.get<UserProfile>("/api/auth/me");
    tokenStorage.setUser(res.data);
    return res.data;
  },

  refreshSession: async () => {
    const refreshToken = tokenStorage.getRefreshToken();

    if (!refreshToken) {
      throw new Error("Khong tim thay refresh token.");
    }

    const res = await axiosClient.post<AuthResponse>("/api/auth/refresh", {
      refreshToken,
    });

    tokenStorage.setSession({
      accessToken: res.data.accessToken,
      refreshToken: res.data.refreshToken,
      user: res.data.user,
    });

    return res.data;
  },

  requestPasswordReset: async (data: ForgotPasswordRequest) => {
    const res = await axiosClient.post<ForgotPasswordResponse>(
      "/api/auth/forgot-password",
      data
    );
    return res.data;
  },

  resetPassword: async (data: ResetPasswordRequest) => {
    const res = await axiosClient.post<MessageResponse>(
      "/api/auth/reset-password",
      data
    );
    return res.data;
  },

  getStoredUser: () => tokenStorage.getUser<UserProfile>(),

  logout: async () => {
    const refreshToken = tokenStorage.getRefreshToken();

    try {
      if (refreshToken) {
        await axiosClient.post("/api/auth/logout", { refreshToken });
      }
    } finally {
      tokenStorage.clearSession();

      if (typeof window !== "undefined") {
        window.location.href = "/login";
      }
    }
  },
};
