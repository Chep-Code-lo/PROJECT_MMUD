import type { UserRole } from "./auth";

export type AdminUser = {
  id: number;
  fullName: string;
  email: string;
  role: UserRole;
  createdAt: string;
};

export type AdminSummary = {
  users: number;
  courses: number;
  activeEnrollments: number;
  certificates: number;
  auditLogs: number;
  publishedCourses: number;
};
