"use client";

import { authService } from "@/services/authService";

export default function Sidebar() {
  return (
    <aside className="w-64 min-h-screen bg-white shadow text-gray-900 p-6">
      <h2 className="text-xl font-bold text-blue-700 mb-8">
        CloudAPI
      </h2>

      <div className="space-y-3">
        <a
          href="/dashboard"
          className="block px-3 py-2 rounded hover:bg-blue-50 hover:text-blue-700"
        >
          Dashboard
        </a>

        <a
          href="/customers"
          className="block px-3 py-2 rounded hover:bg-blue-50 hover:text-blue-700"
        >
          Customers
        </a>

        <a
          href="/customers/new"
          className="block px-3 py-2 rounded hover:bg-blue-50 hover:text-blue-700"
        >
          Create Customer
        </a>

        <a
          href="/tickets"
          className="block px-3 py-2 rounded hover:bg-blue-50 hover:text-blue-700"
        >
          Tickets
        </a>

        <a
          href="/tickets/new"
          className="block px-3 py-2 rounded hover:bg-blue-50 hover:text-blue-700"
        >
          Create Ticket
        </a>

        <a
          href="/audit-logs"
          className="block px-3 py-2 rounded hover:bg-blue-50 hover:text-blue-700"
        >
          Audit Logs
        </a>
      </div>

      <button
        onClick={() => authService.logout()}
        className="mt-8 w-full bg-red-600 text-white px-4 py-2 rounded hover:bg-red-700"
      >
        Logout
      </button>
    </aside>
  );
}