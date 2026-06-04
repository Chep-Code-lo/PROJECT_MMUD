"use client";

import { authService } from "@/services/authService";

export default function DashboardPage() {
  return (
    <main className="min-h-screen bg-gray-100 p-8">
      <div className="max-w-5xl mx-auto">
        <div className="flex justify-between items-center mb-8">
          <h1 className="text-3xl font-bold">
            Small Company Security Dashboard
          </h1>

          <button
            onClick={() => authService.logout()}
            className="bg-red-600 text-white px-4 py-2 rounded"
          >
            Logout
          </button>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <a href="/customers" className="border p-6 rounded-xl bg-white shadow">
            <h2 className="text-xl font-semibold mb-2">Customers</h2>
            <p>Quản lý khách hàng, dữ liệu nhạy cảm được AES ở backend.</p>
          </a>

          <a href="/tickets" className="border p-6 rounded-xl bg-white shadow">
            <h2 className="text-xl font-semibold mb-2">Tickets</h2>
            <p>Tạo và xử lý ticket hỗ trợ.</p>
          </a>

          <a href="/audit-logs" className="border p-6 rounded-xl bg-white shadow">
            <h2 className="text-xl font-semibold mb-2">Audit Logs</h2>
            <p>Admin xem lịch sử hành động quan trọng.</p>
          </a>
        </div>
      </div>
    </main>
  );
}