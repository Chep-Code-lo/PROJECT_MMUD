"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { useEffect, useState } from "react";
import Button from "@/components/Button";
import Input from "@/components/Input";
import ProtectedRoute from "@/components/ProtectedRoute";
import { getApiErrorMessage } from "@/lib/apiError";
import { formatLocalDateTime } from "@/lib/dateTime";
import { adminService } from "@/services/adminService";
import type { AdminCourseRoster } from "@/types/admin";

const currencyFormatter = new Intl.NumberFormat("vi-VN");

export default function AdminCourseRosterPage() {
  const params = useParams<{ courseId: string }>();
  const courseId = Number(params.courseId);

  const [roster, setRoster] = useState<AdminCourseRoster | null>(null);
  const [addEmail, setAddEmail] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [actionKey, setActionKey] = useState("");

  const loadRoster = async () => {
    try {
      const data = await adminService.getCourseRoster(courseId);
      setRoster(data);
      setError("");
    } catch (err) {
      setError(getApiErrorMessage(err, "Không tải được danh sách học viên của khóa học."));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadRoster();
  }, [courseId]);

  const handleApprove = async (enrollmentId: number) => {
    setActionKey(`approve-${enrollmentId}`);
    setError("");

    try {
      await adminService.approveEnrollment(enrollmentId);
      await loadRoster();
    } catch (err) {
      setError(getApiErrorMessage(err, "Không thể duyệt yêu cầu ghi danh."));
    } finally {
      setActionKey("");
    }
  };

  const handleRemove = async (enrollmentId: number) => {
    setActionKey(`remove-${enrollmentId}`);
    setError("");

    try {
      await adminService.removeEnrollment(enrollmentId);
      await loadRoster();
    } catch (err) {
      setError(getApiErrorMessage(err, "Không thể xóa học viên khỏi khóa học."));
    } finally {
      setActionKey("");
    }
  };

  const handleAddStudent = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setActionKey("add-student");
    setError("");

    try {
      await adminService.addStudentToCourse(courseId, addEmail);
      setAddEmail("");
      await loadRoster();
    } catch (err) {
      setError(getApiErrorMessage(err, "Không thể thêm học viên vào khóa học."));
    } finally {
      setActionKey("");
    }
  };

  return (
    <ProtectedRoute allowedRoles={["ADMIN"]}>
      <main className="space-y-6">
        {loading && (
          <div className="rounded-[28px] border border-[#1f2a24]/10 bg-white/85 p-6 text-sm text-[#4f5b54]">
            Đang tải thông tin khóa học...
          </div>
        )}

        {!loading && roster && (
          <>
            <section className="rounded-[34px] border border-[#1f2a24]/10 bg-white/86 p-8 shadow-[0_24px_60px_rgba(31,42,36,0.08)]">
              <div className="flex flex-wrap items-start justify-between gap-4">
                <div className="max-w-3xl">
                  <p className="mb-3 text-xs font-semibold uppercase tracking-[0.32em] text-[#8b5e34]">
                    Quản lý học viên
                  </p>
                  <h1 className="mb-3 text-3xl font-bold text-[#12372f]">
                    {roster.courseTitle}
                  </h1>
                  <p className="mb-4 text-sm leading-7 text-[#536059]">
                    {roster.courseSummary}
                  </p>
                  <div className="space-y-2 text-sm text-[#4f5b54]">
                    <p>Giảng viên: {roster.instructorName}</p>
                    <p>Học phí: {currencyFormatter.format(roster.price)} VND</p>
                    <p>Học viên đang học: {roster.activeStudentCount}</p>
                    <p>Yêu cầu chờ duyệt: {roster.pendingRequestCount}</p>
                  </div>
                </div>

                <Link
                  href="/admin/courses"
                  className="inline-flex rounded-full border border-[#1f2a24]/12 bg-white/80 px-4 py-2.5 text-sm font-semibold text-[#12372f]"
                >
                  Quay lại danh sách khóa học
                </Link>
              </div>
            </section>

            {error && (
              <div className="rounded-[28px] border border-[#b45309]/15 bg-[#fff4ea] p-6 text-sm text-[#9a3412]">
                {error}
              </div>
            )}

            <section className="rounded-[30px] border border-[#1f2a24]/8 bg-white/82 p-6 shadow-[0_18px_40px_rgba(31,42,36,0.06)]">
              <h2 className="mb-4 text-2xl font-semibold text-[#163d35]">
                Thêm học viên vào khóa học
              </h2>
              <form onSubmit={handleAddStudent} className="flex flex-col gap-3 md:flex-row md:items-end">
                <Input
                  label="Email học viên"
                  type="email"
                  placeholder="student2@example.com"
                  value={addEmail}
                  onChange={(event) => setAddEmail(event.target.value)}
                  required
                />
                <Button
                  type="submit"
                  className="md:w-auto"
                  disabled={actionKey === "add-student"}
                >
                  {actionKey === "add-student" ? "Đang thêm..." : "Thêm học viên"}
                </Button>
              </form>
            </section>

            <section className="grid gap-6 xl:grid-cols-2">
              <article className="rounded-[30px] border border-[#1f2a24]/8 bg-white/82 p-6 shadow-[0_18px_40px_rgba(31,42,36,0.06)]">
                <div className="mb-5 flex items-center justify-between gap-3">
                  <h2 className="text-2xl font-semibold text-[#163d35]">
                    Yêu cầu chờ duyệt
                  </h2>
                  <span className="rounded-full bg-[#fff4ea] px-3 py-1 text-xs font-semibold uppercase tracking-[0.16em] text-[#9a3412]">
                    {roster.pendingRequestCount} yêu cầu
                  </span>
                </div>

                <div className="space-y-4">
                  {roster.pendingRequests.length === 0 && (
                    <div className="rounded-2xl border border-[#1f2a24]/8 bg-[#faf6ee] px-4 py-4 text-sm text-[#536059]">
                      Không có yêu cầu nào đang chờ duyệt.
                    </div>
                  )}

                  {roster.pendingRequests.map((enrollment) => (
                    <div
                      key={enrollment.enrollmentId}
                      className="rounded-2xl border border-[#1f2a24]/8 bg-[#faf6ee] px-4 py-4"
                    >
                      <div className="flex flex-wrap items-start justify-between gap-3">
                        <div className="space-y-1 text-sm text-[#526059]">
                          <p className="text-base font-semibold text-[#17352d]">
                            {enrollment.studentName}
                          </p>
                          <p>{enrollment.studentEmail}</p>
                          <p>Tạo yêu cầu: {formatLocalDateTime(enrollment.createdAt)}</p>
                        </div>

                        <div className="flex flex-wrap gap-2">
                          <Link
                            href={`/admin/students/${enrollment.studentId}`}
                            className="inline-flex rounded-full border border-[#1f2a24]/12 bg-white/80 px-4 py-2 text-sm font-semibold text-[#12372f]"
                          >
                            Xem hồ sơ
                          </Link>
                          <Button
                            variant="success"
                            disabled={actionKey === `approve-${enrollment.enrollmentId}`}
                            onClick={() => handleApprove(enrollment.enrollmentId)}
                          >
                            {actionKey === `approve-${enrollment.enrollmentId}`
                              ? "Đang duyệt..."
                              : "Duyệt"}
                          </Button>
                          <Button
                            variant="danger"
                            disabled={actionKey === `remove-${enrollment.enrollmentId}`}
                            onClick={() => handleRemove(enrollment.enrollmentId)}
                          >
                            {actionKey === `remove-${enrollment.enrollmentId}`
                              ? "Đang xóa..."
                              : "Từ chối"}
                          </Button>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </article>

              <article className="rounded-[30px] border border-[#1f2a24]/8 bg-white/82 p-6 shadow-[0_18px_40px_rgba(31,42,36,0.06)]">
                <div className="mb-5 flex items-center justify-between gap-3">
                  <h2 className="text-2xl font-semibold text-[#163d35]">
                    Học viên đang học
                  </h2>
                  <span className="rounded-full bg-[#eef7f4] px-3 py-1 text-xs font-semibold uppercase tracking-[0.16em] text-[#0f766e]">
                    {roster.activeStudentCount} học viên
                  </span>
                </div>

                <div className="space-y-4">
                  {roster.activeStudents.length === 0 && (
                    <div className="rounded-2xl border border-[#1f2a24]/8 bg-[#faf6ee] px-4 py-4 text-sm text-[#536059]">
                      Chưa có học viên nào đang học.
                    </div>
                  )}

                  {roster.activeStudents.map((enrollment) => (
                    <div
                      key={enrollment.enrollmentId}
                      className="rounded-2xl border border-[#1f2a24]/8 bg-[#faf6ee] px-4 py-4"
                    >
                      <div className="flex flex-wrap items-start justify-between gap-3">
                        <div className="space-y-1 text-sm text-[#526059]">
                          <p className="text-base font-semibold text-[#17352d]">
                            {enrollment.studentName}
                          </p>
                          <p>{enrollment.studentEmail}</p>
                          <p>Kích hoạt: {formatLocalDateTime(enrollment.activatedAt)}</p>
                          <p>
                            Chứng chỉ: {enrollment.certificateIssued ? "Đã cấp" : "Chưa cấp"}
                          </p>
                        </div>

                        <div className="flex flex-wrap gap-2">
                          <Link
                            href={`/admin/students/${enrollment.studentId}`}
                            className="inline-flex rounded-full border border-[#1f2a24]/12 bg-white/80 px-4 py-2 text-sm font-semibold text-[#12372f]"
                          >
                            Xem hồ sơ
                          </Link>
                          <Button
                            variant="danger"
                            disabled={actionKey === `remove-${enrollment.enrollmentId}`}
                            onClick={() => handleRemove(enrollment.enrollmentId)}
                          >
                            {actionKey === `remove-${enrollment.enrollmentId}`
                              ? "Đang xóa..."
                              : "Xóa khỏi khóa học"}
                          </Button>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </article>
            </section>
          </>
        )}

        {!loading && !roster && error && (
          <div className="rounded-[28px] border border-[#b45309]/15 bg-[#fff4ea] p-6 text-sm text-[#9a3412]">
            {error}
          </div>
        )}
      </main>
    </ProtectedRoute>
  );
}
