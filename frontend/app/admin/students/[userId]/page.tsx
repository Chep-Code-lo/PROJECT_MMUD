"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { useEffect, useState } from "react";
import ProtectedRoute from "@/components/ProtectedRoute";
import { getApiErrorMessage } from "@/lib/apiError";
import { formatLocalDateTime } from "@/lib/dateTime";
import { adminService } from "@/services/adminService";
import type { AdminStudentProfile } from "@/types/admin";

export default function AdminStudentProfilePage() {
  const params = useParams<{ userId: string }>();
  const userId = Number(params.userId);

  const [profile, setProfile] = useState<AdminStudentProfile | null>(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    adminService
      .getStudentProfile(userId)
      .then(setProfile)
      .catch((err) =>
        setError(getApiErrorMessage(err, "Khong tai duoc ho so hoc vien."))
      )
      .finally(() => setLoading(false));
  }, [userId]);

  return (
    <ProtectedRoute allowedRoles={["ADMIN"]}>
      <main className="space-y-6">
        {loading && (
          <div className="rounded-[28px] border border-[#1f2a24]/10 bg-white/85 p-6 text-sm text-[#4f5b54]">
            Dang tai ho so hoc vien...
          </div>
        )}

        {error && (
          <div className="rounded-[28px] border border-[#b45309]/15 bg-[#fff4ea] p-6 text-sm text-[#9a3412]">
            {error}
          </div>
        )}

        {profile && (
          <section className="rounded-[34px] border border-[#1f2a24]/10 bg-white/86 p-8 shadow-[0_24px_60px_rgba(31,42,36,0.08)]">
            <div className="flex flex-wrap items-start justify-between gap-4">
              <div>
                <p className="mb-3 text-xs font-semibold uppercase tracking-[0.32em] text-[#8b5e34]">
                  Ho so hoc vien
                </p>
                <h1 className="mb-3 text-3xl font-bold text-[#12372f]">
                  {profile.fullName}
                </h1>
                <div className="space-y-2 text-sm leading-7 text-[#536059]">
                  <p>Email: {profile.email}</p>
                  <p>Vai tro: {profile.role}</p>
                  <p>So dien thoai: {profile.phoneNumber ?? "-"}</p>
                  <p>Dia chi thanh toan: {profile.billingAddress ?? "-"}</p>
                  <p>Tao luc: {formatLocalDateTime(profile.createdAt)}</p>
                </div>
              </div>

              <Link
                href="/admin/courses"
                className="inline-flex rounded-full border border-[#1f2a24]/12 bg-white/80 px-4 py-2.5 text-sm font-semibold text-[#12372f]"
              >
                Quay lai quan ly ghi danh
              </Link>
            </div>
          </section>
        )}
      </main>
    </ProtectedRoute>
  );
}
