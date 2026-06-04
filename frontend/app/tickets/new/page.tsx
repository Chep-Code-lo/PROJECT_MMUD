"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { ticketService } from "@/services/ticketService";

export default function NewTicketPage() {
  const router = useRouter();

  const [form, setForm] = useState({
    title: "",
    description: "",
    priority: "MEDIUM",
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    setForm({
      ...form,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    await ticketService.createTicket(form);
    router.push("/tickets");
  };

  return (
    <main className="p-8 max-w-xl">
      <h1 className="text-2xl font-bold mb-6">Create Ticket</h1>

      <form onSubmit={handleSubmit} className="space-y-4">
        <input
          name="title"
          placeholder="Title"
          className="w-full border p-2 rounded"
          onChange={handleChange}
          required
        />

        <textarea
          name="description"
          placeholder="Description"
          className="w-full border p-2 rounded"
          onChange={handleChange}
          required
        />

        <select
          name="priority"
          className="w-full border p-2 rounded"
          onChange={handleChange}
        >
          <option value="LOW">LOW</option>
          <option value="MEDIUM">MEDIUM</option>
          <option value="HIGH">HIGH</option>
        </select>

        <button className="bg-blue-600 text-white px-4 py-2 rounded">
          Save
        </button>
      </form>
    </main>
  );
}