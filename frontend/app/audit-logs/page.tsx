"use client";

import { useEffect, useState } from "react";
import AuditLogTable from "@/components/AuditLogTable";
import Navbar from "@/components/Navbar";
import ProtectedRoute from "@/components/ProtectedRoute";
import { formatLocalDateTime } from "@/lib/dateTime";
import { auditLogService } from "@/services/auditLogService";
import type { AuditLog } from "@/types/audit";

export default function AuditLogsPage() {
  const [logs, setLogs] = useState<AuditLog[]>([]);
  const [error, setError] = useState("");
  const [lastSyncedAt, setLastSyncedAt] = useState<number | null>(null);
  const [displayNow, setDisplayNow] = useState(() => Date.now());

  useEffect(() => {
    let active = true;
    let loading = false;

    const loadLogs = async () => {
      if (loading) {
        return;
      }

      loading = true;

      try {
        const data = await auditLogService.getAuditLogs();
        if (!active) {
          return;
        }

        setLogs(data);
        setError("");
        setLastSyncedAt(Date.now());
      } catch {
        if (active) {
          setError("Khong tai duoc audit logs.");
        }
      } finally {
        loading = false;
      }
    };

    loadLogs();
    const refreshIntervalId = window.setInterval(loadLogs, 3000);
    const clockIntervalId = window.setInterval(() => {
      setDisplayNow(Date.now());
    }, 1000);

    return () => {
      active = false;
      window.clearInterval(refreshIntervalId);
      window.clearInterval(clockIntervalId);
    };
  }, []);

  return (
    <ProtectedRoute allowedRoles={["ADMIN"]}>
      <main className="min-h-screen bg-gray-100 text-gray-900">
        <Navbar />

        <section className="max-w-6xl mx-auto p-8">
          <div className="mb-6">
            <h1 className="text-3xl font-bold">Audit Log Viewer</h1>
            <p className="mt-2 text-gray-700">
              Man hinh nay demo login success/failed va customer action de phuc vu truy vet bao mat.
            </p>
            <p className="mt-2 text-sm text-gray-500">
              Audit log tu dong refresh moi 3 giay. Thoi gian hien thi duoc chot
              theo gio du an `UTC+7`, dong nho ben duoi cho biet hanh dong da xay
              ra cach hien tai bao lau.
            </p>
            <p className="mt-1 text-sm text-gray-500">
              Cap nhat lan cuoi: {formatLocalDateTime(lastSyncedAt)}
            </p>
          </div>

          {error && <p className="mb-4 text-red-600">{error}</p>}

          <AuditLogTable logs={logs} now={displayNow} />
        </section>
      </main>
    </ProtectedRoute>
  );
}
