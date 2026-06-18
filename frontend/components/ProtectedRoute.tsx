"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { authService } from "@/services/authService";
import { tokenStorage } from "@/lib/tokenStorage";

type Role = "ADMIN" | "STAFF" | "USER";

type ProtectedRouteProps = {
  children: React.ReactNode;
  allowedRoles?: Role[];
};

export default function ProtectedRoute({
  children,
  allowedRoles,
}: ProtectedRouteProps) {
  const router = useRouter();

  const [checking, setChecking] = useState(true);
  const [forbidden, setForbidden] = useState(false);

  useEffect(() => {
    const checkAuth = async () => {
      const token = tokenStorage.getToken();

      if (!token) {
        router.push("/login");
        return;
      }

      try {
        const currentUser = await authService.getCurrentUser();

        if (allowedRoles && !allowedRoles.includes(currentUser.role)) {
          setForbidden(true);
          setChecking(false);
          return;
        }

        setChecking(false);
      } catch {
        tokenStorage.removeToken();
        router.push("/login");
      }
    };

    checkAuth();
  }, [router, allowedRoles]);

  if (checking) {
    return (
      <main className="min-h-screen flex items-center justify-center bg-gray-100 text-gray-900">
        <div className="bg-white px-6 py-4 rounded-xl shadow">
          Đang kiểm tra đăng nhập...
        </div>
      </main>
    );
  }

  if (forbidden) {
    return (
      <main className="min-h-screen flex items-center justify-center bg-gray-100 text-gray-900">
        <div className="bg-white p-8 rounded-xl shadow text-center">
          <h1 className="text-2xl font-bold text-red-600 mb-4">
            403 Forbidden
          </h1>

          <p className="mb-4">
            Bạn không có quyền truy cập trang này.
          </p>

          <button
            onClick={() => router.push("/dashboard")}
            className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
          >
            Quay lại Dashboard
          </button>
        </div>
      </main>
    );
  }

  return <>{children}</>;
}
