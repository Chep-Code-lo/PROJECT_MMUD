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

export default function AuditLogTable({
  logs,
  now = Date.now(),
}: AuditLogTableProps) {
  if (logs.length === 0) {
    return (
      <div className="rounded-[28px] border border-[#1f2a24]/10 bg-white/85 p-6 text-center text-sm text-[#5b675f] shadow-[0_18px_40px_rgba(31,42,36,0.08)]">
        Chua co nhat ky nao.
      </div>
    );
  }

  return (
    <div className="overflow-x-auto rounded-[28px] border border-[#1f2a24]/10 bg-white/90 shadow-[0_24px_50px_rgba(31,42,36,0.08)]">
      <table className="w-full border-collapse text-left text-sm text-[#1f2a24]">
        <thead className="bg-[#f4ecdf] text-xs uppercase tracking-[0.18em] text-[#6d6358]">
          <tr>
            <th className="px-4 py-4">Thoi gian</th>
            <th className="px-4 py-4">Hanh dong</th>
            <th className="px-4 py-4">Nguoi thuc hien</th>
            <th className="px-4 py-4">Doi tuong</th>
            <th className="px-4 py-4">Status</th>
            <th className="px-4 py-4">Noi dung</th>
          </tr>
        </thead>
        <tbody>
          {logs.map((log) => (
            <tr
              key={log.id}
              className="border-t border-[#1f2a24]/8 align-top hover:bg-[#fcf7ef]"
            >
              <td
                className="px-4 py-4"
                title={`UTC: ${formatUtcDateTime(log.createdAt)}`}
              >
                <div className="font-medium">
                  {formatLocalDateTime(log.createdAt)}
                </div>
                <div className="text-xs text-[#6a756e]">
                  {formatRelativeTime(log.createdAt, now)}
                </div>
              </td>
              <td className="px-4 py-4 font-semibold">{log.action}</td>
              <td className="px-4 py-4">
                <div>{log.actorEmail ?? "anonymous"}</div>
                <div className="text-xs text-[#6a756e]">{log.ipAddress ?? "-"}</div>
              </td>
              <td className="px-4 py-4">
                {log.targetType ?? "-"}
                {log.targetId ? ` #${log.targetId}` : ""}
              </td>
              <td className="px-4 py-4">
                <span
                  className={`rounded-full px-3 py-1 text-xs font-semibold uppercase tracking-[0.18em] ${
                    log.status === "SUCCESS"
                      ? "bg-[#e7f6ee] text-[#166534]"
                      : "bg-[#fff1e8] text-[#b45309]"
                  }`}
                >
                  {log.status}
                </span>
              </td>
              <td className="px-4 py-4 leading-6 text-[#46524b]">{log.message}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
