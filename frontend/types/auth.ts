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
  role: "ADMIN" | "STAFF" | "USER";
};

export type LoginResponse = {
  token: string;
  user: User;
};