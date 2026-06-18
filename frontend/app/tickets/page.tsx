"use client";

import Navbar from "@/components/Navbar";
import ProtectedRoute from "@/components/ProtectedRoute";

export default function TicketsPage() {
  return (
    <ProtectedRoute allowedRoles={["ADMIN", "STAFF", "USER"]}>
      <main className="min-h-screen bg-gray-100 text-gray-900">
        <Navbar />

        <section className="max-w-5xl mx-auto p-8">
          <div className="bg-white rounded-xl shadow p-8">
            <h1 className="text-3xl font-bold mb-4">Ticket Management</h1>
            <p className="text-gray-700 mb-3">
              Màn hình này dùng để nối với Ticket API thật ở backend.
            </p>
            <p className="text-gray-700">
              Luồng tối thiểu phải có tạo ticket, cập nhật trạng thái và phân biệt
              rõ quyền truy cập giữa ADMIN, STAFF và USER.
            </p>
          </div>
        </section>
      </main>
    </ProtectedRoute>
  );
}
