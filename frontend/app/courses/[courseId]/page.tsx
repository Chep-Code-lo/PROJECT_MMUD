"use client";

import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import Button from "@/components/Button";
import { getApiErrorMessage } from "@/lib/apiError";
import { tokenStorage } from "@/lib/tokenStorage";
import { courseService } from "@/services/courseService";
import type { CheckoutResponse, CourseDetail } from "@/types/course";

const currencyFormatter = new Intl.NumberFormat("vi-VN");

export default function CourseDetailPage() {
  const params = useParams<{ courseId: string }>();
  const router = useRouter();
  const courseId = Number(params.courseId);

  const [course, setCourse] = useState<CourseDetail | null>(null);
  const [checkout, setCheckout] = useState<CheckoutResponse | null>(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [checkingOut, setCheckingOut] = useState(false);

  useEffect(() => {
    courseService
      .getCourse(courseId)
      .then(setCourse)
      .catch((err) =>
        setError(getApiErrorMessage(err, "Khong tai duoc chi tiet khoa hoc."))
      )
      .finally(() => setLoading(false));
  }, [courseId]);

  const handleCheckout = async () => {
    if (!tokenStorage.getAccessToken()) {
      router.push("/login");
      return;
    }

    setCheckingOut(true);
    setError("");

    try {
      const result = await courseService.checkout(courseId);
      setCheckout(result);
      const updatedCourse = await courseService.getCourse(courseId);
      setCourse(updatedCourse);
    } catch (err) {
      setError(getApiErrorMessage(err, "Khong the xu ly yeu cau dang ky."));
    } finally {
      setCheckingOut(false);
    }
  };

  if (loading) {
    return (
      <main className="rounded-[28px] border border-[#1f2a24]/10 bg-white/85 p-6 text-sm text-[#4f5b54]">
        Dang tai chi tiet khoa hoc...
      </main>
    );
  }

  if (!course) {
    return (
      <main className="rounded-[28px] border border-[#b45309]/15 bg-[#fff4ea] p-6 text-sm text-[#9a3412]">
        {error || "Khong tim thay khoa hoc."}
      </main>
    );
  }

  return (
    <main className="space-y-6">
      <section className="rounded-[34px] border border-[#1f2a24]/10 bg-white/86 p-8 shadow-[0_24px_60px_rgba(31,42,36,0.08)]">
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div className="max-w-3xl">
            <p className="mb-3 text-xs font-semibold uppercase tracking-[0.32em] text-[#8b5e34]">
              {course.instructorName}
            </p>
            <h1 className="mb-3 text-3xl font-bold text-[#12372f]">
              {course.title}
            </h1>
            <p className="mb-4 text-sm leading-7 text-[#536059]">
              {course.description}
            </p>
            <div className="text-2xl font-bold text-[#0f766e]">
              {currencyFormatter.format(course.price)} VND
            </div>
          </div>

          <div className="rounded-[28px] border border-[#1f2a24]/10 bg-[#f4ecdf] p-5">
            <div className="text-xs font-semibold uppercase tracking-[0.18em] text-[#6c655a]">
              Trang thai tham gia
            </div>
            <div
              className={`mt-3 rounded-full px-4 py-2 text-sm font-semibold uppercase tracking-[0.16em] ${
                course.enrolled
                  ? "bg-[#e7f6ee] text-[#166534]"
                  : "bg-[#fff4ea] text-[#9a3412]"
              }`}
            >
              {course.enrolled ? "Da ghi danh" : "Chua ghi danh"}
            </div>
            <p className="mt-4 text-sm leading-7 text-[#536059]">
              Sau khi hoan tat dang ky, ban co the mo day du tat ca bai hoc trong khoa hoc nay.
            </p>
            <div className="mt-5">
              <Button onClick={handleCheckout} disabled={checkingOut}>
                {checkingOut ? "Dang xu ly..." : "Dang ky hoc"}
              </Button>
            </div>
          </div>
        </div>
      </section>

      {error && (
        <div className="rounded-[28px] border border-[#b45309]/15 bg-[#fff4ea] p-6 text-sm text-[#9a3412]">
          {error}
        </div>
      )}

      {checkout && (
        <section className="rounded-[30px] border border-[#0f766e]/15 bg-[#eef7f4] p-6 shadow-[0_18px_40px_rgba(15,118,110,0.08)]">
          <p className="mb-2 text-xs font-semibold uppercase tracking-[0.24em] text-[#0f766e]">
            Yeu cau da duoc ghi nhan
          </p>
          <h2 className="mb-3 text-xl font-semibold text-[#16443a]">
            {checkout.courseTitle}
          </h2>
          <div className="space-y-2 text-sm leading-7 text-[#426158]">
            <p>
              Yeu cau tham gia khoa hoc da duoc ghi nhan. Trang thai hien tai:
              {" "}
              {checkout.status === "ACTIVE" ? "Dang hoc" : "Dang xu ly"}.
            </p>
          </div>
        </section>
      )}

      <section className="space-y-4">
        <div>
          <p className="text-xs font-semibold uppercase tracking-[0.28em] text-[#8b5e34]">
            Bai hoc
          </p>
          <h2 className="mt-2 text-2xl font-bold text-[#12372f]">
            Noi dung khoa hoc
          </h2>
        </div>

        <div className="grid gap-4">
          {course.lessons.map((lesson) => (
            <article
              key={lesson.id}
              className="rounded-[28px] border border-[#1f2a24]/8 bg-white/82 p-6 shadow-[0_18px_36px_rgba(31,42,36,0.05)]"
            >
              <div className="flex flex-wrap items-start justify-between gap-3">
                <div>
                  <p className="text-xs font-semibold uppercase tracking-[0.18em] text-[#8b5e34]">
                    Lesson {lesson.sortOrder}
                  </p>
                  <h3 className="mt-2 text-xl font-semibold text-[#163d35]">
                    {lesson.title}
                  </h3>
                </div>
                <span
                  className={`rounded-full px-3 py-1 text-xs font-semibold uppercase tracking-[0.16em] ${
                    lesson.unlocked
                      ? "bg-[#e7f6ee] text-[#166534]"
                      : "bg-[#fff4ea] text-[#9a3412]"
                  }`}
                >
                  {lesson.unlocked ? "Co the xem" : "Xem truoc"}
                </span>
              </div>

              <p className="mt-4 text-sm leading-7 text-[#536059]">
                {lesson.previewText}
              </p>

              <div className="mt-5">
                {lesson.unlocked ? (
                  <Link
                    href={`/courses/${course.id}/lessons/${lesson.id}`}
                    className="inline-flex rounded-full bg-[#12372f] px-4 py-2.5 text-sm font-semibold text-[#f7faf8]"
                  >
                    Xem bai hoc
                  </Link>
                ) : (
                  <div className="text-sm text-[#8c5b3c]">
                    Dang ky khoa hoc de mo noi dung day du.
                  </div>
                )}
              </div>
            </article>
          ))}
        </div>
      </section>
    </main>
  );
}
