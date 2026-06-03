import { axiosClient } from "@/lib/axiosClient";

export type LoginPayload = {
  email: string;
  password: string;
};

export type RegisterPayload = {
  fullName: string;
  email: string;
  password: string;
};

export const authService = {
  login: (payload: LoginPayload) => axiosClient.post("/api/auth/login", payload),
  register: (payload: RegisterPayload) => axiosClient.post("/api/auth/register", payload),
  me: () => axiosClient.get("/api/auth/me")
};

