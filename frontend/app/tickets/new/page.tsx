"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Button from "@/components/Button";
import Input from "@/components/Input";
import Navbar from "@/components/Navbar";
import ProtectedRoute from "@/components/ProtectedRoute";
import { authService } from "@/services/authService";
import { ticketService } from "@/services/ticketService";
import type { TicketPriority } from "@/types/ticket";

export default function NewTicketPage() {
  const router = useRouter();

  const [form, setForm] = useState({
    title: "",
    description: "",
    priority: "MEDIUM" as TicketPriority,
  });
  const [currentUserId, setCurrentUserId] = useState<number | null>(null);
  const [error, setError] = useState("");

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>
  ) => {
    setForm({
      ...form,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!currentUserId) {
      setError("Khong xac dinh duoc nguoi tao ticket.");
      return;
    }

    try {
      await ticketService.createTicket({
        ...form,
        createdById: currentUserId,
      });
      router.push("/tickets");
    } catch {
      setError("Khong tao duoc ticket.");
    }
  };

  useEffect(() => {
    const loadCurrentUser = async () => {
      try {
        const currentUser = await authService.getCurrentUser();
        setCurrentUserId(currentUser.id);
      } catch {
        setError("Khong xac dinh duoc nguoi dung hien tai.");
      }
    };

    loadCurrentUser();
  }, []);

  return (
    <ProtectedRoute allowedRoles={["ADMIN", "STAFF", "USER"]}>
      <main className="min-h-screen bg-gray-100 text-gray-900">
        <Navbar />

        <section className="max-w-xl mx-auto p-8">
          <div className="bg-white rounded-xl shadow p-8">
            <h1 className="text-2xl font-bold mb-6">Create Ticket</h1>

            {error && <p className="mb-4 text-red-600">{error}</p>}

            <form onSubmit={handleSubmit} className="space-y-4">
              <Input
                name="title"
                label="Title"
                placeholder="Title"
                onChange={handleChange}
                required
              />

              <div>
                <label className="block mb-2 text-sm font-medium text-gray-900">
                  Description
                </label>
                <textarea
                  name="description"
                  placeholder="Description"
                  className="w-full border border-gray-300 rounded px-3 py-2 bg-white text-gray-900 outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  onChange={handleChange}
                  required
                />
              </div>

              <div>
                <label className="block mb-2 text-sm font-medium text-gray-900">
                  Priority
                </label>
                <select
                  name="priority"
                  className="w-full border border-gray-300 rounded px-3 py-2 bg-white text-gray-900 outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  onChange={handleChange}
                  value={form.priority}
                >
                  <option value="LOW">LOW</option>
                  <option value="MEDIUM">MEDIUM</option>
                  <option value="HIGH">HIGH</option>
                </select>
              </div>

              <Button type="submit">Save</Button>
            </form>
          </div>
        </section>
      </main>
    </ProtectedRoute>
  );
}
