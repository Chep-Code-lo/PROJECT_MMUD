"use client";

import { useEffect, useState } from "react";
import AuditLogTable from "@/components/AuditLogTable";
import ProtectedRoute from "@/components/ProtectedRoute";
import { formatLocalDateTime } from "@/lib/dateTime";
import { getApiErrorMessage } from "@/lib/apiError";
import { adminService } from "@/services/adminService";
import { auditLogService } from "@/services/auditLogService";
import type { AdminSummary, AdminUser } from "@/types/admin";
import type { AuditLog } from "@/types/audit";

export default function AdminAuditLogsPage() {
  const [summary, setSummary] = useState<AdminSummary | null>(null);
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [logs, setLogs] = useState<AuditLog[]>([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      adminService.getSummary(),
      adminService.getUsers(),
      auditLogService.getAuditLogs(),
    ])
      .then(([summaryData, usersData, logsData]) => {
        setSummary(summaryData);
        setUsers(usersData);
        setLogs(logsData);
      })
      .catch((err) =>
        setError(getApiErrorMessage(err, "Khong tai duoc admin security data."))
      )
      .finally(() => setLoading(false));
  }, []);

  return (
    <ProtectedRoute allowedRoles={["ADMIN"]}>
      <main className="space-y-6">
        <section className="rounded-[34px] border border-[#1f2a24]/10 bg-white/86 p-8 shadow-[0_24px_60px_rgba(31,42,36,0.08)]">
          <p className="mb-3 text-xs font-semibold uppercase tracking-[0.32em] text-[#8b5e34]">
            Admin Audit API
          </p>
          <h1 className="mb-3 text-3xl font-bold text-[#12372f]">
            Audit logs va thong ke bao mat
          </h1>
          <p className="max-w-3xl text-sm leading-7 text-[#536059]">
            Trang nay chi cho ADMIN. Day la man hinh de demo giam sat: login success,
            login failed, access denied, token rejected va webhook accepted/rejected.
          </p>
        </section>

        {loading && (
          <div className="rounded-[28px] border border-[#1f2a24]/10 bg-white/85 p-6 text-sm text-[#4f5b54]">
            Dang tai du lieu admin...
          </div>
        )}

        {error && (
          <div className="rounded-[28px] border border-[#b45309]/15 bg-[#fff4ea] p-6 text-sm text-[#9a3412]">
            {error}
          </div>
        )}

        {summary && (
          <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
            {[
              ["Users", summary.users],
              ["Courses", summary.courses],
              ["Active Enrollments", summary.activeEnrollments],
              ["Certificates", summary.certificates],
              ["Audit Logs", summary.auditLogs],
              ["Published Courses", summary.publishedCourses],
            ].map(([label, value]) => (
              <article
                key={label}
                className="rounded-[28px] border border-[#1f2a24]/8 bg-white/82 p-6 shadow-[0_18px_40px_rgba(31,42,36,0.06)]"
              >
                <div className="text-xs font-semibold uppercase tracking-[0.18em] text-[#8b5e34]">
                  {label}
                </div>
                <div className="mt-3 text-3xl font-bold text-[#12372f]">{value}</div>
              </article>
            ))}
          </section>
        )}

        <section className="rounded-[30px] border border-[#1f2a24]/8 bg-white/82 p-6 shadow-[0_18px_40px_rgba(31,42,36,0.06)]">
          <h2 className="mb-4 text-2xl font-semibold text-[#163d35]">
            Demo users
          </h2>
          <div className="overflow-x-auto">
            <table className="w-full border-collapse text-left text-sm text-[#1f2a24]">
              <thead className="bg-[#f4ecdf] text-xs uppercase tracking-[0.18em] text-[#6d6358]">
                <tr>
                  <th className="px-4 py-4">ID</th>
                  <th className="px-4 py-4">Name</th>
                  <th className="px-4 py-4">Email</th>
                  <th className="px-4 py-4">Role</th>
                  <th className="px-4 py-4">Created</th>
                </tr>
              </thead>
              <tbody>
                {users.map((user) => (
                  <tr
                    key={user.id}
                    className="border-t border-[#1f2a24]/8 hover:bg-[#fcf7ef]"
                  >
                    <td className="px-4 py-4">{user.id}</td>
                    <td className="px-4 py-4 font-semibold">{user.fullName}</td>
                    <td className="px-4 py-4">{user.email}</td>
                    <td className="px-4 py-4">{user.role}</td>
                    <td className="px-4 py-4">{formatLocalDateTime(user.createdAt)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>

        <section>
          <AuditLogTable logs={logs} />
        </section>
      </main>
    </ProtectedRoute>
  );
}
