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
        setError(getApiErrorMessage(err, "Khong tai duoc danh sach khoa hoc quan tri."))
      )
      .finally(() => setLoading(false));
  }, []);

  return (
    <ProtectedRoute allowedRoles={["ADMIN"]}>
      <main className="space-y-6">
        <section className="rounded-[34px] border border-[#1f2a24]/10 bg-white/86 p-8 shadow-[0_24px_60px_rgba(31,42,36,0.08)]">
          <p className="mb-3 text-xs font-semibold uppercase tracking-[0.32em] text-[#8b5e34]">
            Quan ly ghi danh
          </p>
          <h1 className="mb-3 text-3xl font-bold text-[#12372f]">
            Duyet yeu cau hoc vien theo tung khoa hoc
          </h1>
          <p className="max-w-3xl text-sm leading-7 text-[#536059]">
            Admin xem so luong hoc vien dang hoc, so yeu cau cho duyet va di vao
            tung khoa hoc de them, xoa hoac duyet hoc vien.
          </p>
        </section>

        {loading && (
          <div className="rounded-[28px] border border-[#1f2a24]/10 bg-white/85 p-6 text-sm text-[#4f5b54]">
            Dang tai danh sach khoa hoc...
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
                  {course.pendingRequestCount} cho duyet
                </span>
              </div>

              <p className="mb-4 text-sm leading-7 text-[#536059]">{course.summary}</p>

              <div className="space-y-2 text-sm text-[#4f5b54]">
                <p>Giang vien: {course.instructorName}</p>
                <p>Hoc vien dang hoc: {course.activeStudentCount}</p>
                <p>Hoc phi: {currencyFormatter.format(course.price)} VND</p>
              </div>

              <div className="mt-6">
                <Link
                  href={`/admin/courses/${course.id}`}
                  className="inline-flex rounded-full bg-[#12372f] px-4 py-2.5 text-sm font-semibold text-[#f7faf8]"
                >
                  Mo quan ly khoa hoc
                </Link>
              </div>
            </article>
          ))}
        </section>
      </main>
    </ProtectedRoute>
  );
}
