import type { AuditLog } from "@/types/audit";
import {
  formatLocalDateTime,
  formatRelativeTime,
  formatUtcDateTime,
} from "@/lib/dateTime";

type AuditLogTableProps = {
  logs: AuditLog[];
  now?: number;
};

export default function AuditLogTable({ logs, now = Date.now() }: AuditLogTableProps) {
  if (logs.length === 0) {
    return (
      <div className="bg-white p-6 rounded-xl shadow text-gray-900">
        Chưa có audit log nào.
      </div>
    );
  }

  return (
    <div className="overflow-x-auto bg-white rounded-xl shadow">
      <table className="w-full border-collapse text-gray-900">
        <thead>
          <tr className="bg-gray-100 text-left">
            <th className="border p-3">Project Time (UTC+7)</th>
            <th className="border p-3">Action</th>
            <th className="border p-3">Actor</th>
            <th className="border p-3">Entity</th>
            <th className="border p-3">Result</th>
            <th className="border p-3">Details</th>
          </tr>
        </thead>
        <tbody>
          {logs.map((log) => (
            <tr key={log.id} className="hover:bg-gray-50">
              <td
                className="border p-3"
                title={`UTC: ${formatUtcDateTime(log.createdAt)}`}
              >
                <div className="font-medium">
                  {formatLocalDateTime(log.createdAt)}
                </div>
                <div className="text-xs text-gray-500">
                  {formatRelativeTime(log.createdAt, now)}
                </div>
              </td>
              <td className="border p-3 font-medium">{log.action}</td>
              <td className="border p-3">{log.actorEmail ?? "-"}</td>
              <td className="border p-3">
                {log.entityType ? `${log.entityType} #${log.entityId ?? "-"}` : "-"}
              </td>
              <td className="border p-3">
                <span
                  className={`rounded px-2 py-1 text-sm ${
                    log.success
                      ? "bg-green-100 text-green-700"
                      : "bg-red-100 text-red-700"
                  }`}
                >
                  {log.success ? "SUCCESS" : "FAILED"}
                </span>
              </td>
              <td className="border p-3">{log.details ?? "-"}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
