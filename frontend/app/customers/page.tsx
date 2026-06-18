"use client";

import Navbar from "@/components/Navbar";
import ProtectedRoute from "@/components/ProtectedRoute";

export default function CustomersPage() {
  return (
    <ProtectedRoute allowedRoles={["ADMIN", "STAFF"]}>
      <main className="min-h-screen bg-gray-100 text-gray-900">
        <Navbar />

        <section className="max-w-5xl mx-auto p-8">
          <div className="bg-white rounded-xl shadow p-8">
            <h1 className="text-3xl font-bold mb-4">Customer Management</h1>
            <p className="text-gray-700 mb-3">
              Màn hình này dùng để nối với Customer API thật ở backend.
            </p>
            <p className="text-gray-700">
              Dữ liệu nhạy cảm như số điện thoại, địa chỉ và mã số thuế phải được
              mã hóa bằng AES ở phía backend trước khi lưu vào cơ sở dữ liệu.
            </p>
          </div>
        </section>
      </main>
    </ProtectedRoute>
  );
}
