"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
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

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm({
      ...form,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    await customerService.createCustomer(form);
    router.push("/customers");
  };

  return (
    <main className="p-8 max-w-xl">
      <h1 className="text-2xl font-bold mb-6">Create Customer</h1>

      <form onSubmit={handleSubmit} className="space-y-4">
        <input name="name" placeholder="Name" className="w-full border p-2 rounded" onChange={handleChange} required />
        <input name="email" placeholder="Email" className="w-full border p-2 rounded" onChange={handleChange} required />
        <input name="phone" placeholder="Phone" className="w-full border p-2 rounded" onChange={handleChange} required />
        <input name="address" placeholder="Address" className="w-full border p-2 rounded" onChange={handleChange} required />
        <input name="taxCode" placeholder="Tax Code" className="w-full border p-2 rounded" onChange={handleChange} required />

        <button className="bg-blue-600 text-white px-4 py-2 rounded">
          Save
        </button>
      </form>
    </main>
  );
}