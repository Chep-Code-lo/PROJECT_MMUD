import axiosClient from "@/lib/axiosClient";

export type AuditLog = {
  id: number;
  userId: number;
  action: string;
  ipAddress: string;
  createdAt: string;
};

export type AdminUser = {
  id: number;
  fullName: string;
  email: string;
  role: "ADMIN" | "STAFF" | "USER";
  enabled: boolean;
  createdAt?: string;
};

export const adminService = {
  getAuditLogs: async () => {
    const res = await axiosClient.get<AuditLog[]>("/api/audit-logs");
    return res.data;
  },

  getUsers: async () => {
    const res = await axiosClient.get<AdminUser[]>("/api/admin/users");
    return res.data;
  },

  updateUserRole: async (userId: number, role: "ADMIN" | "STAFF" | "USER") => {
    const res = await axiosClient.put<AdminUser>(`/api/admin/users/${userId}/role`, {
      role,
    });
    return res.data;
  },

  disableUser: async (userId: number) => {
    const res = await axiosClient.put<AdminUser>(`/api/admin/users/${userId}/disable`);
    return res.data;
  },

  enableUser: async (userId: number) => {
    const res = await axiosClient.put<AdminUser>(`/api/admin/users/${userId}/enable`);
    return res.data;
  },
};