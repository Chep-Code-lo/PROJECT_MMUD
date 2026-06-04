"use client";

import { useEffect, useState } from "react";
import { customerService } from "@/services/customerService";
import type { Customer } from "@/types/customer";

export default function CustomersPage() {
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [error, setError] = useState("");

  const loadCustomers = async () => {
    try {
      const data = await customerService.getCustomers();
      setCustomers(data);
    } catch {
      setError("Không tải được danh sách khách hàng. Có thể bạn chưa đăng nhập hoặc không đủ quyền.");
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm("Bạn có chắc muốn xóa khách hàng này?")) return;

    await customerService.deleteCustomer(id);
    loadCustomers();
  };

  useEffect(() => {
    loadCustomers();
  }, []);

  return (
    <main className="p-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">Customers</h1>
        <a href="/customers/new" className="bg-blue-600 text-white px-4 py-2 rounded">
          Add Customer
        </a>
      </div>

      {error && <p className="text-red-600 mb-4">{error}</p>}

      <table className="w-full border bg-white">
        <thead>
          <tr className="bg-gray-100">
            <th className="border p-2">Name</th>
            <th className="border p-2">Email</th>
            <th className="border p-2">Phone</th>
            <th className="border p-2">Address</th>
            <th className="border p-2">Tax Code</th>
            <th className="border p-2">Action</th>
          </tr>
        </thead>

        <tbody>
          {customers.map((c) => (
            <tr key={c.id}>
              <td className="border p-2">{c.name}</td>
              <td className="border p-2">{c.email}</td>
              <td className="border p-2">{c.phone}</td>
              <td className="border p-2">{c.address}</td>
              <td className="border p-2">{c.taxCode}</td>
              <td className="border p-2">
                <button
                  onClick={() => handleDelete(c.id)}
                  className="text-red-600 underline"
                >
                  Delete
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </main>
  );
}