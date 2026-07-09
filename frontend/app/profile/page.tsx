"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import ProtectedRoute from "@/components/ProtectedRoute";
import { formatLocalDateTime } from "@/lib/dateTime";
import { getApiErrorMessage } from "@/lib/apiError";
import { authService } from "@/services/authService";
import { certificateService } from "@/services/certificateService";
import { enrollmentService } from "@/services/enrollmentService";
import type { UserProfile } from "@/types/auth";
import type { Certificate, Enrollment } from "@/types/course";

export default function ProfilePage() {
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [enrollments, setEnrollments] = useState<Enrollment[]>([]);
  const [certificates, setCertificates] = useState<Certificate[]>([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  const loadData = async () => {
    try {
      const user = await authService.getCurrentUser();
      setProfile(user);

      if (user.role === "ADMIN") {
        setEnrollments([]);
        setCertificates([]);
        setError("");
        return;
      }

      const [enrollmentList, certificateList] = await Promise.all([
        enrollmentService.getMine(),
        certificateService.getMine(),
      ]);

      setEnrollments(enrollmentList);
      setCertificates(certificateList);
      setError("");
    } catch (err) {
      setError(getApiErrorMessage(err, "Không tải được thông tin cá nhân."));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const enrollmentStatusLabel: Record<Enrollment["status"], string> = {
    ACTIVE: "Đang học",
    PENDING: "Đang xử lý",
    CANCELLED: "Đã hủy",
  };

  return (
    <ProtectedRoute>
      <main className="space-y-6">
        {loading && (
          <div className="rounded-[28px] border border-[#1f2a24]/10 bg-white/85 p-6 text-sm text-[#4f5b54]">
            Đang tải thông tin tài khoản...
          </div>
        )}

        {error && (
          <div className="rounded-[28px] border border-[#b45309]/15 bg-[#fff4ea] p-6 text-sm text-[#9a3412]">
            {error}
          </div>
        )}

        <section className="rounded-[34px] border border-[#1f2a24]/10 bg-white/86 p-8 shadow-[0_24px_60px_rgba(31,42,36,0.08)]">
          <div className="flex flex-wrap items-start justify-between gap-4">
            <div>
              <p className="mb-3 text-xs font-semibold uppercase tracking-[0.32em] text-[#8b5e34]">
                Hồ sơ cá nhân
              </p>
              <h1 className="mb-3 text-3xl font-bold text-[#12372f]">
                Thông tin tài khoản
              </h1>
              {profile && (
                <div className="space-y-2 text-sm leading-7 text-[#536059]">
                  <p>Họ tên: {profile.fullName}</p>
                  <p>Email: {profile.email}</p>
                  <p>Vai trò: {profile.role === "ADMIN" ? "Quản trị viên" : "Học viên"}</p>
                  <p>Số điện thoại: {profile.phoneNumber ?? "-"}</p>
                  <p>Địa chỉ: {profile.billingAddress ?? "-"}</p>
                  <p>Tạo lúc: {formatLocalDateTime(profile.createdAt)}</p>
                </div>
              )}
            </div>
          </div>
        </section>

        {profile?.role === "ADMIN" ? (
          <section className="grid gap-6 lg:grid-cols-2">
            <article className="rounded-[30px] border border-[#1f2a24]/8 bg-white/82 p-6 shadow-[0_18px_40px_rgba(31,42,36,0.06)]">
              <h2 className="mb-4 text-2xl font-semibold text-[#163d35]">
                Chức năng quản trị
              </h2>
              <p className="mb-5 text-sm leading-7 text-[#536059]">
                Tài khoản quản trị viên không sử dụng luồng học viên. Quản trị viên tập trung
                duyệt ghi danh, quản lý học viên theo từng khóa học và giám sát nhật ký an ninh.
              </p>
              <div className="flex flex-wrap gap-3">
                <Link
                  href="/admin/courses"
                  className="inline-flex min-h-12 items-center justify-center rounded-full bg-[#0f766e] px-6 py-3 text-base font-bold tracking-wide text-white shadow-[0_16px_34px_rgba(15,118,110,0.28)] transition duration-300 hover:-translate-y-0.5 hover:bg-[#115e59] hover:shadow-[0_20px_42px_rgba(15,118,110,0.34)] active:translate-y-0"
                >
                  Quản lý ghi danh
                </Link>
                <Link
                  href="/admin/audit-logs"
                  className="inline-flex rounded-full border border-[#1f2a24]/12 bg-white/80 px-4 py-2.5 text-sm font-semibold text-[#12372f]"
                >
                  Xem nhật ký kiểm tra
                </Link>
              </div>
            </article>

            <article className="rounded-[30px] border border-[#1f2a24]/8 bg-white/82 p-6 shadow-[0_18px_40px_rgba(31,42,36,0.06)]">
              <h2 className="mb-4 text-2xl font-semibold text-[#163d35]">
                Vai trò hiện tại
              </h2>
              <div className="space-y-3 text-sm leading-7 text-[#536059]">
                <p>Quản trị viên có thể duyệt yêu cầu đăng ký học và thêm học viên vào khóa học.</p>
                <p>Quản trị viên có thể xem danh sách học viên của từng khóa học và hồ sơ học viên.</p>
                <p>Quản trị viên có thể theo dõi các sự kiện bảo mật trong nhật ký kiểm tra.</p>
              </div>
            </article>
          </section>
        ) : (
          <section className="grid gap-6 lg:grid-cols-2">
          <article className="rounded-[30px] border border-[#1f2a24]/8 bg-white/82 p-6 shadow-[0_18px_40px_rgba(31,42,36,0.06)]">
            <h2 className="mb-4 text-2xl font-semibold text-[#163d35]">
              Khóa học đã đăng ký
            </h2>
            <div className="space-y-3">
              {enrollments.length === 0 && (
                <div className="rounded-2xl border border-[#1f2a24]/8 bg-[#faf6ee] px-4 py-4 text-sm text-[#536059]">
                  Bạn chưa có khóa học nào.
                </div>
              )}

              {enrollments.map((enrollment) => (
                <div
                  key={enrollment.id}
                  className="rounded-2xl border border-[#1f2a24]/8 bg-[#faf6ee] px-4 py-4"
                >
                  <div className="flex flex-wrap items-center justify-between gap-3">
                    <div className="font-semibold text-[#17352d]">
                      {enrollment.courseTitle}
                    </div>
                    <span
                      className={`rounded-full px-3 py-1 text-xs font-semibold uppercase tracking-[0.16em] ${
                        enrollment.status === "ACTIVE"
                          ? "bg-[#e7f6ee] text-[#166534]"
                          : "bg-[#fff4ea] text-[#9a3412]"
                      }`}
                    >
                      {enrollmentStatusLabel[enrollment.status]}
                    </span>
                  </div>
                  <div className="mt-2 text-sm leading-7 text-[#526059]">
                    {enrollment.activatedAt
                      ? `Kích hoạt lúc: ${formatLocalDateTime(enrollment.activatedAt)}`
                      : "Đang chờ xác nhận."}
                  </div>
                </div>
              ))}
            </div>
          </article>

          <article className="rounded-[30px] border border-[#1f2a24]/8 bg-white/82 p-6 shadow-[0_18px_40px_rgba(31,42,36,0.06)]">
            <h2 className="mb-4 text-2xl font-semibold text-[#163d35]">
              Chứng chỉ và điểm số
            </h2>
            <div className="space-y-3">
              {certificates.length === 0 && (
                <div className="rounded-2xl border border-[#1f2a24]/8 bg-[#faf6ee] px-4 py-4 text-sm text-[#536059]">
                  Chưa có chứng chỉ nào.
                </div>
              )}

              {certificates.map((certificate) => (
                <div
                  key={certificate.id}
                  className="rounded-2xl border border-[#1f2a24]/8 bg-[#faf6ee] px-4 py-4"
                >
                  <div className="font-semibold text-[#17352d]">
                    {certificate.courseTitle}
                  </div>
                  <div className="mt-2 text-sm leading-7 text-[#526059]">
                    Mã chứng chỉ: {certificate.certificateCode}
                    <br />
                    Điểm số: {certificate.score}
                    <br />
                    Cấp lúc: {formatLocalDateTime(certificate.issuedAt)}
                  </div>
                </div>
              ))}
            </div>
          </article>
          </section>
        )}
      </main>
    </ProtectedRoute>
  );
}
