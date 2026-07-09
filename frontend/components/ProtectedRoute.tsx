"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { tokenStorage } from "@/lib/tokenStorage";
import { authService } from "@/services/authService";
import type { UserRole } from "@/types/auth";

type ProtectedRouteProps = {
  children: React.ReactNode;
  allowedRoles?: UserRole[];
};

export default function ProtectedRoute({
  children,
  allowedRoles,
}: ProtectedRouteProps) {
  const pathname = usePathname();
  const router = useRouter();
  const [checking, setChecking] = useState(true);
  const [forbidden, setForbidden] = useState(false);

  useEffect(() => {
    let mounted = true;

    const checkAuth = async () => {
      const token = tokenStorage.getAccessToken();

      if (!token) {
        router.push("/login");
        return;
      }

      try {
        const currentUser = await authService.getCurrentUser();

        if (allowedRoles && !allowedRoles.includes(currentUser.role)) {
          if (mounted) {
            setForbidden(true);
            setChecking(false);
          }
          return;
        }

        if (mounted) {
          setChecking(false);
        }
      } catch {
        tokenStorage.clearSession();
        router.push("/login");
      }
    };

    checkAuth();

    return () => {
      mounted = false;
    };
  }, [allowedRoles, pathname, router]);

  if (checking) {
    return (
      <main className="flex min-h-[55vh] items-center justify-center text-[#1f2a24]">
        <div className="rounded-[28px] border border-[#1f2a24]/10 bg-white/85 px-6 py-4 shadow-[0_18px_40px_rgba(31,42,36,0.08)]">
          Đang kiểm tra phiên đăng nhập...
        </div>
      </main>
    );
  }

  if (forbidden) {
    return (
      <main className="flex min-h-[55vh] items-center justify-center text-[#1f2a24]">
        <div className="max-w-xl rounded-[32px] border border-[#1f2a24]/10 bg-white/90 p-8 text-center shadow-[0_22px_50px_rgba(31,42,36,0.09)]">
          <p className="mb-3 text-xs font-semibold uppercase tracking-[0.28em] text-[#b45309]">
            Truy cập bị từ chối
          </p>
          <h1 className="mb-4 text-2xl font-bold text-[#9a3412]">
            Bạn không có quyền xem nội dung này
          </h1>

          <p className="mb-6 text-sm leading-7 text-[#4f5b54]">
            Vui lòng quay lại trang phù hợp hoặc đăng nhập bằng tài khoản khác.
          </p>

          <Link
            href="/courses"
            className="inline-flex rounded-full bg-[#0f766e] px-5 py-3 text-sm font-semibold text-[#f7faf8] shadow-[0_16px_32px_rgba(15,118,110,0.22)]"
          >
            Quay về khóa học
          </Link>
        </div>
      </main>
    );
  }

  return <>{children}</>;
}
