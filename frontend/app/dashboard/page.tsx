"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import ProtectedRoute from "@/components/ProtectedRoute";
import { getApiErrorMessage } from "@/lib/apiError";
import { authService } from "@/services/authService";
import type { UserProfile } from "@/types/auth";

export default function DashboardPage() {
  const [currentUser, setCurrentUser] = useState<UserProfile | null>(null);
  const [error, setError] = useState("");

  const quickLinks = [
    {
      title: "Khoa hoc",
      href: "/courses",
      text: "Xem danh muc khoa hoc va tiep tuc hanh trinh hoc tap cua ban.",
    },
    {
      title: "Ho so",
      href: "/profile",
      text: "Theo doi thong tin tai khoan, cac khoa hoc da dang ky va chung chi.",
    },
  ];

  if (currentUser?.role === "ADMIN") {
    quickLinks.push({
      title: "Quan tri",
      href: "/admin/audit-logs",
      text: "Theo doi nguoi dung, thong ke va nhat ky hoat dong cua he thong.",
    });
  }

  useEffect(() => {
    authService
      .getCurrentUser()
      .then(setCurrentUser)
      .catch((err) => setError(getApiErrorMessage(err, "Khong tai duoc user hien tai.")));
  }, []);

  return (
    <ProtectedRoute>
      <main className="space-y-6">
        <section className="rounded-[34px] border border-[#1f2a24]/10 bg-white/86 p-8 shadow-[0_24px_60px_rgba(31,42,36,0.08)]">
          <p className="mb-3 text-xs font-semibold uppercase tracking-[0.32em] text-[#8b5e34]">
            Tong quan tai khoan
          </p>
          <h1 className="mb-3 text-3xl font-bold text-[#12372f]">
            Chao mung ban quay tro lai
          </h1>
          <p className="max-w-3xl text-sm leading-7 text-[#526059]">
            Day la khu vuc tong quan de ban truy cap nhanh vao khoa hoc, ho so va
            mot so chuc nang quan tri neu tai khoan cua ban duoc cap quyen.
          </p>

          {currentUser && (
            <div className="mt-6 rounded-[26px] border border-[#1f2a24]/8 bg-[#f4ecdf] p-5">
              <div className="text-sm font-semibold uppercase tracking-[0.18em] text-[#6c655a]">
                Tai khoan hien tai
              </div>
              <div className="mt-3 text-lg font-semibold text-[#17352d]">
                {currentUser.fullName} / {currentUser.role}
              </div>
              <div className="text-sm text-[#5a645d]">{currentUser.email}</div>
            </div>
          )}

          {error && (
            <div className="mt-5 rounded-2xl border border-[#b45309]/15 bg-[#fff4ea] px-4 py-3 text-sm text-[#9a3412]">
              {error}
            </div>
          )}
        </section>

        <section className="grid gap-4 md:grid-cols-3">
          {quickLinks.map((item) => (
            <Link
              key={item.title}
              href={item.href}
              className="rounded-[28px] border border-[#1f2a24]/8 bg-white/80 p-6 shadow-[0_18px_40px_rgba(31,42,36,0.06)] transition hover:-translate-y-0.5 hover:shadow-[0_24px_45px_rgba(31,42,36,0.08)]"
            >
              <h2 className="mb-3 text-xl font-semibold text-[#163d35]">{item.title}</h2>
              <p className="text-sm leading-7 text-[#536059]">{item.text}</p>
            </Link>
          ))}
        </section>
      </main>
    </ProtectedRoute>
  );
}
