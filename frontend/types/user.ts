export type UserRole = "ADMIN" | "STAFF" | "USER";

export type User = {
  id: number;
  fullName: string;
  email: string;
  role: UserRole;
};

