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

const actionLabels: Record<string, string> = {
  LOGIN_SUCCESS: "Đăng nhập thành công",
  LOGIN_FAILED: "Đăng nhập thất bại",
  LOGOUT: "Đăng xuất",
  REGISTER: "Đăng ký tài khoản",
  PASSWORD_RESET_REQUEST: "Yêu cầu đặt lại mật khẩu",
  PASSWORD_RESET: "Đặt lại mật khẩu",
  COURSE_CHECKOUT: "Gửi yêu cầu ghi danh",
  ENROLLMENT_APPROVED: "Duyệt ghi danh",
  ENROLLMENT_REMOVED: "Xóa ghi danh",
  STUDENT_ADDED: "Thêm học viên",
  WEBHOOK_RECEIVED: "Nhận yêu cầu đồng bộ",
  RATE_LIMIT_EXCEEDED: "Vượt giới hạn tần suất",
  ACCESS_DENIED: "Từ chối truy cập",
};

const targetLabels: Record<string, string> = {
  USER: "Người dùng",
  COURSE: "Khóa học",
  ENROLLMENT: "Ghi danh",
  LESSON: "Bài học",
  CERTIFICATE: "Chứng chỉ",
  WEBHOOK: "Yêu cầu đồng bộ",
  AUTH: "Xác thực",
  SYSTEM: "Hệ thống",
};

function formatAuditAction(action: string) {
  return actionLabels[action] ?? action.replaceAll("_", " ").toLowerCase();
}

function formatAuditTarget(targetType?: string | null) {
  if (!targetType) {
    return "-";
  }

  return targetLabels[targetType] ?? targetType.replaceAll("_", " ").toLowerCase();
}

export default function AuditLogTable({
  logs,
  now = Date.now(),
}: AuditLogTableProps) {
  if (logs.length === 0) {
    return (
      <div className="rounded-[28px] border border-[#1f2a24]/10 bg-white/85 p-6 text-center text-sm text-[#5b675f] shadow-[0_18px_40px_rgba(31,42,36,0.08)]">
        Chưa có nhật ký nào.
      </div>
    );
  }

  return (
    <div className="overflow-x-auto rounded-[28px] border border-[#1f2a24]/10 bg-white/90 shadow-[0_24px_50px_rgba(31,42,36,0.08)]">
      <table className="w-full border-collapse text-left text-sm text-[#1f2a24]">
        <thead className="bg-[#f4ecdf] text-xs uppercase tracking-[0.18em] text-[#6d6358]">
          <tr>
            <th className="px-4 py-4">Thời gian</th>
            <th className="px-4 py-4">Hành động</th>
            <th className="px-4 py-4">Người thực hiện</th>
            <th className="px-4 py-4">Đối tượng</th>
            <th className="px-4 py-4">Trạng thái</th>
            <th className="px-4 py-4">Nội dung</th>
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
              <td className="px-4 py-4 font-semibold">{formatAuditAction(log.action)}</td>
              <td className="px-4 py-4">
                <div>{log.actorEmail ?? "Không xác định"}</div>
                <div className="text-xs text-[#6a756e]">{log.ipAddress ?? "-"}</div>
              </td>
              <td className="px-4 py-4">
                {formatAuditTarget(log.targetType)}
                {log.targetId ? ` #${log.targetId}` : ""}
              </td>
              <td className="px-4 py-4">
                <span
                  className={`inline-flex min-w-28 items-center justify-center rounded-full px-4 py-2 text-center text-sm font-bold leading-none ${
                    log.status === "SUCCESS"
                      ? "bg-[#e7f6ee] text-[#166534]"
                      : "bg-[#fff1e8] text-[#b45309]"
                  }`}
                >
                  {log.status === "SUCCESS" ? "Thành công" : "Thất bại"}
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
