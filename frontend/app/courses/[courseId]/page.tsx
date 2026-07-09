"use client";

import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import Button from "@/components/Button";
import { getApiErrorMessage } from "@/lib/apiError";
import { tokenStorage } from "@/lib/tokenStorage";
import { authService } from "@/services/authService";
import { courseService } from "@/services/courseService";
import type { CourseDetail, EnrollmentRequestResponse } from "@/types/course";

const currencyFormatter = new Intl.NumberFormat("vi-VN");

export default function CourseDetailPage() {
  const params = useParams<{ courseId: string }>();
  const router = useRouter();
  const courseId = Number(params.courseId);

  const [course, setCourse] = useState<CourseDetail | null>(null);
  const [enrollmentRequest, setEnrollmentRequest] =
    useState<EnrollmentRequestResponse | null>(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [submittingRequest, setSubmittingRequest] = useState(false);

  useEffect(() => {
    const storedUser = authService.getStoredUser();
    if (storedUser?.role === "ADMIN") {
      router.replace("/admin/courses");
      return;
    }

    courseService
      .getCourse(courseId)
      .then(setCourse)
      .catch((err) =>
        setError(getApiErrorMessage(err, "Không tải được chi tiết khóa học."))
      )
      .finally(() => setLoading(false));
  }, [courseId, router]);

  const handleRequestEnrollment = async () => {
    if (!tokenStorage.getAccessToken()) {
      router.push("/login");
      return;
    }

    setSubmittingRequest(true);
    setError("");

    try {
      const result = await courseService.requestEnrollment(courseId);
      setEnrollmentRequest(result);
      const updatedCourse = await courseService.getCourse(courseId);
      setCourse(updatedCourse);
    } catch (err) {
      setError(getApiErrorMessage(err, "Không thể xử lý yêu cầu đăng ký."));
    } finally {
      setSubmittingRequest(false);
    }
  };

  if (loading) {
    return (
      <main className="rounded-[28px] border border-[#1f2a24]/10 bg-white/85 p-6 text-sm text-[#4f5b54]">
        Đang tải chi tiết khóa học...
      </main>
    );
  }

  if (!course) {
    return (
      <main className="rounded-[28px] border border-[#b45309]/15 bg-[#fff4ea] p-6 text-sm text-[#9a3412]">
        {error || "Không tìm thấy khóa học."}
      </main>
    );
  }

  const hasPendingRequest = enrollmentRequest?.status === "PENDING";
  const accessLabel = course.enrolled
    ? "Đã ghi danh"
    : hasPendingRequest
      ? "Chờ duyệt"
      : "Chưa ghi danh";
  const accessBadgeClass = course.enrolled
    ? "bg-[#e7f6ee] text-[#166534]"
    : hasPendingRequest
      ? "bg-[#fff7e6] text-[#a16207]"
      : "bg-[#fff4ea] text-[#9a3412]";
  const actionLabel = course.enrolled
    ? "Đã có quyền truy cập"
    : hasPendingRequest
      ? "Yêu cầu đã gửi"
      : submittingRequest
        ? "Đang gửi..."
        : "Đăng ký học";

  return (
    <main className="space-y-6">
      <section className="rounded-[34px] border border-[#1f2a24]/10 bg-white/86 p-8 shadow-[0_24px_60px_rgba(31,42,36,0.08)]">
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div className="max-w-3xl">
            <h1 className="mb-4 text-4xl font-bold leading-tight text-[#12372f] md:text-5xl">
              {course.title}
            </h1>
            <p className="mb-5 text-lg leading-8 text-[#536059]">
              {course.description}
            </p>
            <div className="text-2xl font-bold text-[#0f766e]">
              {currencyFormatter.format(course.price)} VND
            </div>
          </div>

          <div className="w-full rounded-[28px] border border-[#1f2a24]/10 bg-white/90 p-6 shadow-[0_18px_42px_rgba(31,42,36,0.08)] backdrop-blur md:w-[320px]">
            <div className="flex items-center justify-between gap-3">
              <div className="text-xl font-bold text-[#12372f]">
                Truy cập khóa học
              </div>
              <div
<<<<<<< HEAD
                className={`shrink-0 rounded-full px-3 py-1.5 text-sm font-bold ${accessBadgeClass}`}
              >
                {accessLabel}
=======
                className={`shrink-0 rounded-full px-3 py-1.5 text-sm font-bold ${
                  course.enrolled
                    ? "bg-[#e7f6ee] text-[#166534]"
                    : "bg-[#fff4ea] text-[#9a3412]"
                }`}
              >
                {course.enrolled ? "Đã ghi danh" : "Chưa ghi danh"}
>>>>>>> 9473e804e5d1cac22a0a761f54d7ef4e3ad72109
              </div>
            </div>

            <div
              className="mt-5 h-px bg-gradient-to-r from-transparent via-[#1f2a24]/12 to-transparent"
            />

            <p className="mt-5 text-base leading-7 text-[#536059]">
              Đăng nhập để gửi yêu cầu tham gia và truy cập khóa học bằng tài khoản của bạn.
            </p>
            <div className="mt-5">
<<<<<<< HEAD
              <Button
                className="w-full"
                onClick={handleRequestEnrollment}
                disabled={submittingRequest || course.enrolled || hasPendingRequest}
              >
                {actionLabel}
=======
              <Button className="w-full" onClick={handleCheckout} disabled={checkingOut}>
                {checkingOut ? "Đang xử lý..." : "Đăng ký học"}
>>>>>>> 9473e804e5d1cac22a0a761f54d7ef4e3ad72109
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

      {enrollmentRequest && (
        <section className="rounded-[30px] border border-[#0f766e]/15 bg-[#eef7f4] p-6 shadow-[0_18px_40px_rgba(15,118,110,0.08)]">
          <p className="mb-2 text-xs font-semibold uppercase tracking-[0.24em] text-[#0f766e]">
            Yêu cầu đã được ghi nhận
          </p>
          <h2 className="mb-3 text-xl font-semibold text-[#16443a]">
            {enrollmentRequest.courseTitle}
          </h2>
          <p className="text-sm leading-7 text-[#426158]">
            Bạn đã gửi yêu cầu tham gia thành công. Tài khoản quản trị viên sẽ duyệt yêu cầu này trước khi mở quyền học.
          </p>
        </section>
      )}

      <section className="space-y-4">
        <div>
          <p className="text-xs font-semibold uppercase tracking-[0.28em] text-[#8b5e34]">
            Bài học
          </p>
          <h2 className="mt-2 text-2xl font-bold text-[#12372f]">
            Nội dung khóa học
          </h2>
        </div>

        <div className="grid gap-4">
          {course.lessons.map((lesson) => (
            <article
              key={lesson.id}
              className="rounded-[28px] border border-[#1f2a24]/8 bg-white/82 p-6 shadow-[0_18px_36px_rgba(31,42,36,0.05)]"
            >
              <p className="text-xs font-semibold uppercase tracking-[0.18em] text-[#8b5e34]">
                Bài {lesson.sortOrder}
              </p>
              <h3 className="mt-2 text-xl font-semibold text-[#163d35]">
                {lesson.title}
              </h3>

              {course.enrolled && (
                <div className="mt-5">
                  <Link
                    href={`/courses/${course.id}/lessons/${lesson.id}`}
                    className="inline-flex min-h-12 items-center justify-center rounded-full bg-[#0f766e] px-6 py-3 text-base font-bold tracking-wide text-[#f9f7f1] shadow-[0_16px_34px_rgba(15,118,110,0.28)] transition duration-300 hover:-translate-y-0.5 hover:bg-[#115e59] hover:shadow-[0_20px_42px_rgba(15,118,110,0.34)] active:translate-y-0"
                  >
                    Mở bài học
                  </Link>
                </div>
              )}
            </article>
          ))}
        </div>
      </section>
    </main>
  );
}
