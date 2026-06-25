"use client";

import { useEffect, useState } from "react";
import AuditLogTable from "@/components/AuditLogTable";
import ProtectedRoute from "@/components/ProtectedRoute";
import { getApiErrorMessage } from "@/lib/apiError";
import { auditLogService } from "@/services/auditLogService";
import type { AuditLog } from "@/types/audit";

export default function AdminAuditLogsPage() {
  const [logs, setLogs] = useState<AuditLog[]>([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    auditLogService
      .getAuditLogs()
      .then((logsData) => {
        setLogs(logsData);
      })
      .catch((err) =>
        setError(getApiErrorMessage(err, "Khong tai duoc nhat ky an ninh."))
      )
      .finally(() => setLoading(false));
  }, []);

  return (
    <ProtectedRoute allowedRoles={["ADMIN"]}>
      <main className="space-y-6">
        <section className="rounded-[34px] border border-[#1f2a24]/10 bg-white/86 p-8 shadow-[0_24px_60px_rgba(31,42,36,0.08)]">
          <p className="mb-3 text-xs font-semibold uppercase tracking-[0.32em] text-[#8b5e34]">
            Giam sat an ninh
          </p>
          <h1 className="mb-3 text-3xl font-bold text-[#12372f]">
            Nhat ky an ninh
          </h1>
          <p className="max-w-3xl text-sm leading-7 text-[#536059]">
            Khu vuc nay chi giu lai phan phuc vu do an Mat ma ung dung:
            theo doi dang nhap, truy cap bi tu choi, webhook, rate limit va cac
            su kien bao mat khac.
          </p>
        </section>

        {loading && (
          <div className="rounded-[28px] border border-[#1f2a24]/10 bg-white/85 p-6 text-sm text-[#4f5b54]">
            Dang tai nhat ky an ninh...
          </div>
        )}

        {error && (
          <div className="rounded-[28px] border border-[#b45309]/15 bg-[#fff4ea] p-6 text-sm text-[#9a3412]">
            {error}
          </div>
        )}

        <section>
          <AuditLogTable logs={logs} />
        </section>
      </main>
    </ProtectedRoute>
  );
}
