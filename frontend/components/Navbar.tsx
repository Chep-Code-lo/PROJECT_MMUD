"use client";

import { authService } from "@/services/authService";

export default function Navbar() {
  return (
    <nav className="bg-white shadow px-8 py-4 flex justify-between items-center text-gray-900">
      <a href="/dashboard" className="font-bold text-xl text-blue-700">
        CloudAPI Security
      </a>

      <div className="flex items-center gap-4">
        <a href="/dashboard" className="hover:text-blue-600">
          Dashboard
        </a>

        <a href="/customers" className="hover:text-blue-600">
          Customers
        </a>

        <a href="/tickets" className="hover:text-blue-600">
          Tickets
        </a>

        <a href="/audit-logs" className="hover:text-blue-600">
          Audit Logs
        </a>

        <button
          onClick={() => authService.logout()}
          className="bg-red-600 text-white px-4 py-2 rounded hover:bg-red-700"
        >
          Logout
        </button>
      </div>
    </nav>
  );
}