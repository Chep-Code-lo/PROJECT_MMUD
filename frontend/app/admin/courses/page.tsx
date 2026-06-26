"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import ProtectedRoute from "@/components/ProtectedRoute";
import { getApiErrorMessage } from "@/lib/apiError";
import { adminService } from "@/services/adminService";
import type { AdminCourseOverview } from "@/types/admin";

const currencyFormatter = new Intl.NumberFormat("vi-VN");

export default function AdminCoursesPage() {
  const [courses, setCourses] = useState<AdminCourseOverview[]>([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    adminService
      .getCourses()
      .then(setCourses)
      .catch((err) =>
        setError(getApiErrorMessage(err, "Không tải được danh sách khóa học quản trị."))
      )
      .finally(() => setLoading(false));
  }, []);

  return (
    <ProtectedRoute allowedRoles={["ADMIN"]}>
      <main className="space-y-6">
        <section className="rounded-[34px] border border-[#1f2a24]/10 bg-white/86 p-8 shadow-[0_24px_60px_rgba(31,42,36,0.08)]">
          <p className="mb-3 text-xs font-semibold uppercase tracking-[0.32em] text-[#8b5e34]">
            Quản lý ghi danh
          </p>
          <h1 className="mb-3 text-3xl font-bold text-[#12372f]">
            Duyệt yêu cầu học viên theo từng khóa học
          </h1>
          <p className="max-w-3xl text-sm leading-7 text-[#536059]">
            Quản trị viên xem số lượng học viên đang học, số yêu cầu chờ duyệt và đi vào
            từng khóa học để thêm, xóa hoặc duyệt học viên.
          </p>
        </section>

        {loading && (
          <div className="rounded-[28px] border border-[#1f2a24]/10 bg-white/85 p-6 text-sm text-[#4f5b54]">
            Đang tải danh sách khóa học...
          </div>
        )}

        {error && (
          <div className="rounded-[28px] border border-[#b45309]/15 bg-[#fff4ea] p-6 text-sm text-[#9a3412]">
            {error}
          </div>
        )}

        <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          {courses.map((course) => (
            <article
              key={course.id}
              className="rounded-[28px] border border-[#1f2a24]/8 bg-white/82 p-6 shadow-[0_18px_40px_rgba(31,42,36,0.06)]"
            >
              <div className="mb-4 flex items-start justify-between gap-3">
                <h2 className="text-xl font-semibold text-[#163d35]">{course.title}</h2>
                <span className="rounded-full bg-[#eef7f4] px-3 py-1 text-xs font-semibold uppercase tracking-[0.16em] text-[#0f766e]">
                  {course.pendingRequestCount} chờ duyệt
                </span>
              </div>

              <p className="mb-4 text-sm leading-7 text-[#536059]">{course.summary}</p>

              <div className="space-y-2 text-sm text-[#4f5b54]">
                <p>Giảng viên: {course.instructorName}</p>
                <p>Học viên đang học: {course.activeStudentCount}</p>
                <p>Học phí: {currencyFormatter.format(course.price)} VND</p>
              </div>

              <div className="mt-6">
                <Link
                  href={`/admin/courses/${course.id}`}
                  className="inline-flex min-h-12 items-center justify-center rounded-full bg-[#0f766e] px-6 py-3 text-base font-bold tracking-wide text-white shadow-[0_16px_34px_rgba(15,118,110,0.28)] transition duration-300 hover:-translate-y-0.5 hover:bg-[#115e59] hover:shadow-[0_20px_42px_rgba(15,118,110,0.34)] active:translate-y-0"
                >
                  Mở quản lý khóa học
                </Link>
              </div>
            </article>
          ))}
        </section>
      </main>
    </ProtectedRoute>
  );
}
