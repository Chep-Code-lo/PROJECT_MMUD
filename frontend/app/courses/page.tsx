"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { getApiErrorMessage } from "@/lib/apiError";
import { authService } from "@/services/authService";
import { courseService } from "@/services/courseService";
import type { CourseSummary } from "@/types/course";

const currencyFormatter = new Intl.NumberFormat("vi-VN");

export default function CoursesPage() {
  const router = useRouter();
  const [courses, setCourses] = useState<CourseSummary[]>([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const storedUser = authService.getStoredUser();
    if (storedUser?.role === "ADMIN") {
      router.replace("/admin/courses");
      return;
    }

    courseService
      .getCourses()
      .then(setCourses)
      .catch((err) =>
        setError(getApiErrorMessage(err, "Không tải được danh sách khóa học."))
      )
      .finally(() => setLoading(false));
  }, [router]);

  return (
    <main className="space-y-6">
      <section className="rounded-[34px] border border-[#1f2a24]/10 bg-white/86 p-8 shadow-[0_24px_60px_rgba(31,42,36,0.08)]">
        <p className="mb-3 text-xs font-semibold uppercase tracking-[0.32em] text-[#8b5e34]">
          Danh mục khóa học
        </p>
        <h1 className="mb-3 text-3xl font-bold text-[#12372f]">
          Khám phá các khóa học hiện có
        </h1>
        <p className="max-w-3xl text-sm leading-7 text-[#536059]">
          Bạn có thể xem thông tin tổng quan, học phí và nội dung khai giảng trước
          khi đăng ký học.
        </p>
      </section>

      {loading && (
        <div className="rounded-[28px] border border-[#1f2a24]/10 bg-white/85 p-6 text-sm text-[#4f5b54]">
          Đang tải khóa học...
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
            <h2 className="mb-4 text-xl font-semibold text-[#163d35]">
              {course.title}
            </h2>

            <p className="mb-6 text-sm leading-7 text-[#536059]">{course.summary}</p>

            <div className="mb-6 text-2xl font-bold text-[#0f766e]">
              {currencyFormatter.format(course.price)} VND
            </div>

            <Link
              href={`/courses/${course.id}`}
              className="inline-flex min-h-12 items-center justify-center rounded-full bg-[#0f766e] px-6 py-3 text-base font-bold tracking-wide text-white shadow-[0_16px_34px_rgba(15,118,110,0.28)] transition duration-300 hover:-translate-y-0.5 hover:bg-[#115e59] hover:shadow-[0_20px_42px_rgba(15,118,110,0.34)] active:translate-y-0"
            >
              Xem chi tiết khóa học
            </Link>
          </article>
        ))}
      </section>
    </main>
  );
}
