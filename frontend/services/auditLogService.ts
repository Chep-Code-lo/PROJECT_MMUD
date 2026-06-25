import axiosClient from "@/lib/axiosClient";
import type { AuditLog } from "@/types/audit";

export const auditLogService = {
  getAuditLogs: async () => {
    const res = await axiosClient.get<AuditLog[]>("/api/admin/audit-logs");
    return res.data;
  },
};
