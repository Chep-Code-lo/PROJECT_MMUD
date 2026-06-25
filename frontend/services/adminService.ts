import axiosClient from "@/lib/axiosClient";
import type { AdminSummary, AdminUser } from "@/types/admin";

export const adminService = {
  getSummary: async () => {
    const res = await axiosClient.get<AdminSummary>("/api/admin/summary");
    return res.data;
  },

  getUsers: async () => {
    const res = await axiosClient.get<AdminUser[]>("/api/admin/users");
    return res.data;
  },
};
