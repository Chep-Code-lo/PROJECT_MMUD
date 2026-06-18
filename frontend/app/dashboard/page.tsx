"use client";

import { useEffect, useState } from "react";
import ProtectedRoute from "@/components/ProtectedRoute";
import Navbar from "@/components/Navbar";
import { authService } from "@/services/authService";
import type { User } from "@/types/auth";

export default function DashboardPage() {
  const [currentUser, setCurrentUser] = useState<User | null>(null);

  useEffect(() => {
    const loadCurrentUser = async () => {
      try {
        const user = await authService.getCurrentUser();
        setCurrentUser(user);
      } catch {
        setCurrentUser(null);
      }
    };

    loadCurrentUser();
  }, []);

  return (
    <ProtectedRoute allowedRoles={["ADMIN", "STAFF", "USER"]}>
      <main className="min-h-screen bg-gray-100 text-gray-900">
        <Navbar />

        <div className="max-w-5xl mx-auto p-8">
          <h1 className="text-3xl font-bold mb-8">
            Small Company Security Dashboard
          </h1>

          {currentUser && (
            <div className="mb-6 rounded-xl border bg-white p-5 shadow">
              <p className="text-sm uppercase tracking-wide text-gray-500">
                Current Session
              </p>
              <h2 className="mt-2 text-xl font-semibold">{currentUser.fullName}</h2>
              <p className="mt-1 text-gray-700">
                {currentUser.email} - role {currentUser.role}
              </p>
            </div>
          )}

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="border p-6 rounded-xl bg-white shadow">
              <h2 className="text-xl font-semibold mb-2">Security Scope</h2>
              <p>
                Dashboard nay tap trung vao JWT, bcrypt, AES-GCM cho customer
                data va audit log de phuc vu huong mat ma ung dung.
              </p>
            </div>

            {(currentUser?.role === "ADMIN" || currentUser?.role === "STAFF") && (
              <a href="/customers" className="border p-6 rounded-xl bg-white shadow">
                <h2 className="text-xl font-semibold mb-2">Customers</h2>
                <p>Quản lý khách hàng, dữ liệu nhạy cảm được AES ở backend.</p>
              </a>
            )}

            {currentUser?.role === "ADMIN" && (
              <a href="/audit-logs" className="border p-6 rounded-xl bg-white shadow">
                <h2 className="text-xl font-semibold mb-2">Audit Logs</h2>
                <p>Xem lịch sử login va customer action de demo truy vet bao mat.</p>
              </a>
            )}
          </div>
        </div>
      </main>
    </ProtectedRoute>
  );
}
