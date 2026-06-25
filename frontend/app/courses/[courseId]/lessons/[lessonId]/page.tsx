"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { useEffect, useState } from "react";
import ProtectedRoute from "@/components/ProtectedRoute";
import { getApiErrorMessage } from "@/lib/apiError";
import { courseService } from "@/services/courseService";
import type { LessonDetail } from "@/types/course";

export default function LessonDetailPage() {
  const params = useParams<{ courseId: string; lessonId: string }>();
  const courseId = Number(params.courseId);
  const lessonId = Number(params.lessonId);

  const [lesson, setLesson] = useState<LessonDetail | null>(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    courseService
      .getLesson(courseId, lessonId)
      .then(setLesson)
      .catch((err) => setError(getApiErrorMessage(err, "Khong tai duoc lesson.")))
      .finally(() => setLoading(false));
  }, [courseId, lessonId]);

  return (
    <ProtectedRoute>
      <main className="space-y-6">
        {loading && (
          <section className="rounded-[28px] border border-[#1f2a24]/10 bg-white/85 p-6 text-sm text-[#4f5b54]">
            Dang tai lesson...
          </section>
        )}

        {!loading && error && (
          <section className="rounded-[30px] border border-[#b45309]/15 bg-[#fff4ea] p-6 text-sm text-[#9a3412]">
            <div className="font-semibold">Khong the mo bai hoc.</div>
            <p className="mt-2 leading-7">{error}</p>
            <Link
              href={`/courses/${courseId}`}
              className="mt-4 inline-flex rounded-full bg-[#12372f] px-4 py-2 text-sm font-semibold text-[#f7faf8]"
            >
              Quay lai khoa hoc
            </Link>
          </section>
        )}

        {lesson && (
          <>
            <section className="rounded-[34px] border border-[#1f2a24]/10 bg-white/86 p-8 shadow-[0_24px_60px_rgba(31,42,36,0.08)]">
              <p className="mb-3 text-xs font-semibold uppercase tracking-[0.32em] text-[#8b5e34]">
                {lesson.courseTitle}
              </p>
              <h1 className="mb-3 text-3xl font-bold text-[#12372f]">
                {lesson.title}
              </h1>
              <p className="text-sm leading-7 text-[#536059]">{lesson.previewText}</p>
            </section>

            <section className="rounded-[34px] border border-[#1f2a24]/10 bg-[#f9f7f2] p-8 shadow-[0_24px_60px_rgba(31,42,36,0.06)]">
              <div className="mb-4 text-xs font-semibold uppercase tracking-[0.24em] text-[#0f766e]">
                Noi dung bai hoc
              </div>
              <div className="whitespace-pre-wrap text-sm leading-8 text-[#33433c]">
                {lesson.content}
              </div>
            </section>
          </>
        )}
      </main>
    </ProtectedRoute>
  );
}
