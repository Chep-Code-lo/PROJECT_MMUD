"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import ProtectedRoute from "@/components/ProtectedRoute";
import { getApiErrorMessage } from "@/lib/apiError";
import { authService } from "@/services/authService";
import type { UserProfile } from "@/types/auth";

const quickLinks = [
  {
    title: "Course Catalogue",
    href: "/courses",
    text: "Kiem tra public courses, lesson preview va checkout mock.",
  },
  {
    title: "Profile + Certificates",
    href: "/profile",
    text: "Xem thong tin da giai ma va danh sach enrollment/certificate cua chinh minh.",
  },
  {
    title: "Swagger UI",
    href: "/swagger-ui.html",
    text: "Dang token vao Swagger de goi API va test role-based access.",
  },
];

export default function DashboardPage() {
  const [currentUser, setCurrentUser] = useState<UserProfile | null>(null);
  const [error, setError] = useState("");

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
            Security Dashboard
          </p>
          <h1 className="mb-3 text-3xl font-bold text-[#12372f]">
            Tong quan flow demo bao mat
          </h1>
          <p className="max-w-3xl text-sm leading-7 text-[#526059]">
            Dashboard nay khong tap trung UI. Muc tieu la giup giang vien nhin nhanh
            cac flow: login JWT, lesson locked/unlocked, certificate ownership, audit
            log va webhook thanh toan.
          </p>

          {currentUser && (
            <div className="mt-6 rounded-[26px] border border-[#1f2a24]/8 bg-[#f4ecdf] p-5">
              <div className="text-sm font-semibold uppercase tracking-[0.18em] text-[#6c655a]">
                Session hien tai
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
