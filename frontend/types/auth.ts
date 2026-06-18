export type UserRole = "ADMIN" | "STAFF" | "USER";

export type LoginRequest = {
  email: string;
  password: string;
};

export type RegisterRequest = {
  fullName: string;
  email: string;
  password: string;
};

export type User = {
  id: number;
  fullName: string;
  email: string;
  role: UserRole;
};

export type LoginResponse = {
  accessToken?: string;
  token?: string;
  tokenType?: string;
  role?: UserRole;
  user: User;
};
