"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Button from "@/components/Button";
import Input from "@/components/Input";
import Navbar from "@/components/Navbar";
import ProtectedRoute from "@/components/ProtectedRoute";
import { customerService } from "@/services/customerService";

export default function NewCustomerPage() {
  const router = useRouter();

  const [form, setForm] = useState({
    name: "",
    email: "",
    phone: "",
    address: "",
    taxCode: "",
  });
  const [error, setError] = useState("");

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm({
      ...form,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    try {
      await customerService.createCustomer(form);
      router.push("/customers");
    } catch {
      setError("Khong tao duoc customer.");
    }
  };

  return (
    <ProtectedRoute allowedRoles={["ADMIN", "STAFF"]}>
      <main className="min-h-screen bg-gray-100 text-gray-900">
        <Navbar />

        <section className="max-w-xl mx-auto p-8">
          <div className="bg-white rounded-xl shadow p-8">
            <h1 className="text-2xl font-bold mb-6">Create Customer</h1>

            {error && <p className="mb-4 text-red-600">{error}</p>}

            <form onSubmit={handleSubmit} className="space-y-4">
              <Input
                name="name"
                label="Name"
                placeholder="Name"
                onChange={handleChange}
                required
              />
              <Input
                name="email"
                label="Email"
                type="email"
                placeholder="Email"
                onChange={handleChange}
                required
              />
              <Input
                name="phone"
                label="Phone"
                placeholder="Phone"
                onChange={handleChange}
                required
              />
              <Input
                name="address"
                label="Address"
                placeholder="Address"
                onChange={handleChange}
                required
              />
              <Input
                name="taxCode"
                label="Tax Code"
                placeholder="Tax Code"
                onChange={handleChange}
                required
              />

              <Button type="submit">Save</Button>
            </form>
          </div>
        </section>
      </main>
    </ProtectedRoute>
  );
}
