"use client";

import { useEffect, useState } from "react";
import Button from "@/components/Button";
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
  const [refreshing, setRefreshing] = useState(false);

  const loadData = async () => {
    try {
      const [user, enrollmentList, certificateList] = await Promise.all([
        authService.getCurrentUser(),
        enrollmentService.getMine(),
        certificateService.getMine(),
      ]);

      setProfile(user);
      setEnrollments(enrollmentList);
      setCertificates(certificateList);
      setError("");
    } catch (err) {
      setError(getApiErrorMessage(err, "Khong tai duoc thong tin ca nhan."));
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleRefreshToken = async () => {
    setRefreshing(true);

    try {
      await authService.refreshSession();
      await loadData();
    } catch (err) {
      setError(getApiErrorMessage(err, "Lam moi access token that bai."));
    } finally {
      setRefreshing(false);
    }
  };

  return (
    <ProtectedRoute>
      <main className="space-y-6">
        {error && (
          <div className="rounded-[28px] border border-[#b45309]/15 bg-[#fff4ea] p-6 text-sm text-[#9a3412]">
            {error}
          </div>
        )}

        <section className="rounded-[34px] border border-[#1f2a24]/10 bg-white/86 p-8 shadow-[0_24px_60px_rgba(31,42,36,0.08)]">
          <div className="flex flex-wrap items-start justify-between gap-4">
            <div>
              <p className="mb-3 text-xs font-semibold uppercase tracking-[0.32em] text-[#8b5e34]">
                User Profile API
              </p>
              <h1 className="mb-3 text-3xl font-bold text-[#12372f]">
                Thong tin ca nhan da giai ma
              </h1>
              {profile && (
                <div className="space-y-2 text-sm leading-7 text-[#536059]">
                  <p>Ho ten: {profile.fullName}</p>
                  <p>Email: {profile.email}</p>
                  <p>Role: {profile.role}</p>
                  <p>Phone: {profile.phoneNumber ?? "-"}</p>
                  <p>Billing address: {profile.billingAddress ?? "-"}</p>
                  <p>Tao luc: {formatLocalDateTime(profile.createdAt)}</p>
                </div>
              )}
            </div>

            <div className="rounded-[28px] border border-[#1f2a24]/8 bg-[#f4ecdf] p-5">
              <div className="mb-4 text-xs font-semibold uppercase tracking-[0.18em] text-[#6c655a]">
                Session tools
              </div>
              <Button onClick={handleRefreshToken} disabled={refreshing}>
                {refreshing ? "Dang refresh..." : "Refresh access token"}
              </Button>
              <p className="mt-4 max-w-sm text-sm leading-7 text-[#5d645b]">
                Nut nay goi /api/auth/refresh de doi access token moi tu refresh token.
              </p>
            </div>
          </div>
        </section>

        <section className="grid gap-6 lg:grid-cols-2">
          <article className="rounded-[30px] border border-[#1f2a24]/8 bg-white/82 p-6 shadow-[0_18px_40px_rgba(31,42,36,0.06)]">
            <h2 className="mb-4 text-2xl font-semibold text-[#163d35]">
              Enrollments
            </h2>
            <div className="space-y-3">
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
                      {enrollment.status}
                    </span>
                  </div>
                  <div className="mt-2 text-sm leading-7 text-[#526059]">
                    Enrollment ID: {enrollment.id}
                  </div>
                </div>
              ))}
            </div>
          </article>

          <article className="rounded-[30px] border border-[#1f2a24]/8 bg-white/82 p-6 shadow-[0_18px_40px_rgba(31,42,36,0.06)]">
            <h2 className="mb-4 text-2xl font-semibold text-[#163d35]">
              Certificates / Score
            </h2>
            <div className="space-y-3">
              {certificates.length === 0 && (
                <div className="rounded-2xl border border-[#1f2a24]/8 bg-[#faf6ee] px-4 py-4 text-sm text-[#536059]">
                  Chua co certificate nao.
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
                    Certificate ID: {certificate.id}
                    <br />
                    Code: {certificate.certificateCode}
                    <br />
                    Score: {certificate.score}
                  </div>
                </div>
              ))}
            </div>
          </article>
        </section>
      </main>
    </ProtectedRoute>
  );
}
