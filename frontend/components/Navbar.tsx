"use client";

import { useEffect, useState } from "react";
import { authService } from "@/services/authService";
import type { User } from "@/types/auth";

export default function Navbar() {
  const [currentUser, setCurrentUser] = useState<User | null>(null);

  useEffect(() => {
    let mounted = true;

    const loadCurrentUser = async () => {
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
  }, []);

  const canManageCustomers =
    currentUser?.role === "ADMIN" || currentUser?.role === "STAFF";
  const isAdmin = currentUser?.role === "ADMIN";

  return (
    <nav className="bg-white shadow px-8 py-4 flex flex-col gap-4 md:flex-row md:justify-between md:items-center text-gray-900">
      <a href="/dashboard" className="font-bold text-xl text-blue-700 shrink-0">
        CloudAPI Security
      </a>

      <div className="flex flex-col gap-3 md:flex-row md:items-center md:gap-4">
        <a href="/dashboard" className="hover:text-blue-600">
          Dashboard
        </a>

        {canManageCustomers && (
          <a href="/customers" className="hover:text-blue-600">
            Customers
          </a>
        )}

        {isAdmin && (
          <a href="/audit-logs" className="hover:text-blue-600">
            Audit Logs
          </a>
        )}

        {currentUser && (
          <div className="text-sm text-gray-600">
            {currentUser.fullName} ({currentUser.role})
          </div>
        )}

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
