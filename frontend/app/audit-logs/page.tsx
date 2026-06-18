"use client";

import { useEffect, useState } from "react";
import AuditLogTable from "@/components/AuditLogTable";
import Navbar from "@/components/Navbar";
import ProtectedRoute from "@/components/ProtectedRoute";
import { auditLogService } from "@/services/auditLogService";
import type { AuditLog } from "@/types/audit";

export default function AuditLogsPage() {
  const [logs, setLogs] = useState<AuditLog[]>([]);
  const [error, setError] = useState("");

  useEffect(() => {
    const loadLogs = async () => {
      try {
        const data = await auditLogService.getAuditLogs();
        setLogs(data);
        setError("");
      } catch {
        setError("Khong tai duoc audit logs.");
      }
    };

    loadLogs();
  }, []);

  return (
    <ProtectedRoute allowedRoles={["ADMIN"]}>
      <main className="min-h-screen bg-gray-100 text-gray-900">
        <Navbar />

        <section className="max-w-6xl mx-auto p-8">
          <div className="mb-6">
            <h1 className="text-3xl font-bold">Audit Log Viewer</h1>
            <p className="mt-2 text-gray-700">
              Màn hình này giúp demo Stage 8: login success/failed, customer action và ticket action.
            </p>
          </div>

          {error && <p className="mb-4 text-red-600">{error}</p>}

          <AuditLogTable logs={logs} />
        </section>
      </main>
    </ProtectedRoute>
  );
}
