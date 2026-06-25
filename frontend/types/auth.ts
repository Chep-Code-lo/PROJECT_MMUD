export type UserRole = "STUDENT" | "INSTRUCTOR" | "ADMIN";

export type LoginRequest = {
  email: string;
  password: string;
};

export type RegisterRequest = {
  fullName: string;
  email: string;
  password: string;
  phoneNumber?: string;
  billingAddress?: string;
};

export type UserProfile = {
  id: number;
  fullName: string;
  email: string;
  role: UserRole;
  phoneNumber?: string | null;
  billingAddress?: string | null;
  createdAt?: string;
};

export type AuthResponse = {
  accessToken: string;
  refreshToken?: string | null;
  tokenType: string;
  accessTokenExpiresInSeconds: number;
  refreshTokenExpiresInSeconds: number;
  role: UserRole;
  scope: string;
  user: UserProfile;
};
