"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { getApiErrorMessage } from "@/lib/apiError";
import { courseService } from "@/services/courseService";
import type { CourseSummary } from "@/types/course";

const currencyFormatter = new Intl.NumberFormat("vi-VN");

export default function CoursesPage() {
  const [courses, setCourses] = useState<CourseSummary[]>([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    courseService
      .getCourses()
      .then(setCourses)
      .catch((err) =>
        setError(getApiErrorMessage(err, "Khong tai duoc danh sach khoa hoc."))
      )
      .finally(() => setLoading(false));
  }, []);

  return (
    <main className="space-y-6">
      <section className="rounded-[34px] border border-[#1f2a24]/10 bg-white/86 p-8 shadow-[0_24px_60px_rgba(31,42,36,0.08)]">
        <p className="mb-3 text-xs font-semibold uppercase tracking-[0.32em] text-[#8b5e34]">
          Public Course API
        </p>
        <h1 className="mb-3 text-3xl font-bold text-[#12372f]">
          Danh sach khoa hoc cong khai
        </h1>
        <p className="max-w-3xl text-sm leading-7 text-[#536059]">
          Student co the xem public courses ma chua can dang nhap. Muon xem full lesson
          content thi phai co enrollment ACTIVE hoac la instructor/admin.
        </p>
      </section>

      {loading && (
        <div className="rounded-[28px] border border-[#1f2a24]/10 bg-white/85 p-6 text-sm text-[#4f5b54]">
          Dang tai khoa hoc...
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
              <div>
                <p className="text-xs font-semibold uppercase tracking-[0.18em] text-[#8b5e34]">
                  {course.instructorName}
                </p>
                <h2 className="mt-2 text-xl font-semibold text-[#163d35]">
                  {course.title}
                </h2>
              </div>
              <span className="rounded-full bg-[#eff8f4] px-3 py-1 text-xs font-semibold uppercase tracking-[0.16em] text-[#14532d]">
                public
              </span>
            </div>

            <p className="mb-6 text-sm leading-7 text-[#536059]">{course.summary}</p>

            <div className="mb-6 text-2xl font-bold text-[#0f766e]">
              {currencyFormatter.format(course.price)} VND
            </div>

            <Link
              href={`/courses/${course.id}`}
              className="inline-flex rounded-full bg-[#12372f] px-4 py-2.5 text-sm font-semibold text-[#f7faf8]"
            >
              Xem chi tiet khoa hoc
            </Link>
          </article>
        ))}
      </section>
    </main>
  );
}
