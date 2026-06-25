"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useEffect, useState } from "react";
import { tokenStorage } from "@/lib/tokenStorage";
import { authService } from "@/services/authService";
import type { UserProfile } from "@/types/auth";

export default function Navbar() {
  const pathname = usePathname();
  const [currentUser, setCurrentUser] = useState<UserProfile | null>(null);

  useEffect(() => {
    let mounted = true;

    const loadCurrentUser = async () => {
      if (!tokenStorage.getAccessToken()) {
        if (mounted) {
          setCurrentUser(null);
        }
        return;
      }

      try {
        const user = await authService.getCurrentUser();
        if (mounted) {
          setCurrentUser(user);
        }
      } catch {
        if (mounted) {
          setCurrentUser(null);
        }
      }
    };

    loadCurrentUser();

    return () => {
      mounted = false;
    };
  }, [pathname]);

  const isAdmin = currentUser?.role === "ADMIN";

  return (
    <nav className="sticky top-0 z-30 border-b border-[#1f2a24]/10 bg-[#fff9f1]/85 px-6 py-4 backdrop-blur md:px-10">
      <div className="mx-auto flex max-w-6xl flex-col gap-4 md:flex-row md:items-center md:justify-between">
        <Link href="/" className="shrink-0">
          <span className="block text-xs font-semibold uppercase tracking-[0.34em] text-[#8b5e34]">
            Online Learning
          </span>
          <span className="text-xl font-bold text-[#12372f]">CourseHub</span>
        </Link>

        <div className="flex flex-col gap-3 text-sm font-medium text-[#30413a] md:flex-row md:items-center md:gap-5">
          <Link href="/courses" className="transition hover:text-[#0f766e]">
            Khoa hoc
          </Link>

          {currentUser && (
            <Link href="/dashboard" className="transition hover:text-[#0f766e]">
              Tong quan
            </Link>
          )}

          {currentUser && (
            <Link href="/profile" className="transition hover:text-[#0f766e]">
              Ho so
            </Link>
          )}

          {isAdmin && (
            <Link
              href="/admin/audit-logs"
              className="transition hover:text-[#0f766e]"
            >
              Quan tri
            </Link>
          )}

          {currentUser ? (
            <>
              <div className="rounded-full border border-[#1f2a24]/10 bg-white/80 px-4 py-2 text-xs uppercase tracking-[0.18em] text-[#5b675f]">
                {currentUser.fullName} / {currentUser.role}
              </div>

              <button
                onClick={() => authService.logout()}
                className="rounded-full bg-[#b45309] px-4 py-2 text-sm font-semibold text-[#fffaf2] shadow-[0_16px_30px_rgba(180,83,9,0.18)] transition hover:bg-[#92400e]"
              >
                Dang xuat
              </button>
            </>
          ) : (
            <div className="flex items-center gap-3">
              <Link
                href="/login"
                className="rounded-full border border-[#1f2a24]/12 px-4 py-2 transition hover:bg-white/70"
              >
                Dang nhap
              </Link>
              <Link
                href="/register"
                className="rounded-full bg-[#0f766e] px-4 py-2 text-[#f7faf8] shadow-[0_16px_30px_rgba(15,118,110,0.2)] transition hover:bg-[#115e59]"
              >
                Dang ky
              </Link>
            </div>
          )}
        </div>
      </div>
    </nav>
  );
}
