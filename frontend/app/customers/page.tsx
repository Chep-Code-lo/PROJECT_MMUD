"use client";

import { useEffect, useState } from "react";
import CustomerTable from "@/components/CustomerTable";
import Navbar from "@/components/Navbar";
import ProtectedRoute from "@/components/ProtectedRoute";
import { customerService } from "@/services/customerService";
import type { Customer } from "@/types/customer";

export default function CustomersPage() {
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [error, setError] = useState("");

  const loadCustomers = async () => {
    try {
      const data = await customerService.getCustomers();
      setCustomers(data);
      setError("");
    } catch {
      setError("Khong tai duoc danh sach khach hang. Co the ban chua dang nhap hoac khong du quyen.");
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm("Ban co chac muon xoa khach hang nay?")) return;

    try {
      await customerService.deleteCustomer(id);
      await loadCustomers();
    } catch {
      setError("Khong xoa duoc khach hang.");
    }
  };

  useEffect(() => {
    loadCustomers();
  }, []);

  return (
    <ProtectedRoute allowedRoles={["ADMIN", "STAFF"]}>
      <main className="min-h-screen bg-gray-100 text-gray-900">
        <Navbar />

        <section className="max-w-5xl mx-auto p-8">
          <div className="flex justify-between items-center mb-6 gap-4">
            <div>
              <h1 className="text-3xl font-bold">Customer Management</h1>
              <p className="text-gray-700 mt-2">
                Du lieu nhay cam nhu so dien thoai, dia chi va ma so thue phai
                duoc ma hoa bang AES o backend truoc khi luu vao co so du lieu.
              </p>
            </div>

            <a
              href="/customers/new"
              className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
            >
              Add Customer
            </a>
          </div>

          {error && <p className="text-red-600 mb-4">{error}</p>}

          <CustomerTable customers={customers} onDelete={handleDelete} />
        </section>
      </main>
    </ProtectedRoute>
  );
}
