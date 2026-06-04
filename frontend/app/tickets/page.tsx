"use client";

import { useEffect, useState } from "react";
import { ticketService } from "@/services/ticketService";
import type { Ticket, TicketStatus } from "@/types/ticket";

export default function TicketsPage() {
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [error, setError] = useState("");

  const loadTickets = async () => {
    try {
      const data = await ticketService.getTickets();
      setTickets(data);
    } catch {
      setError("Không tải được ticket. Có thể bạn chưa đăng nhập hoặc không đủ quyền.");
    }
  };

  const changeStatus = async (id: number, status: TicketStatus) => {
    await ticketService.updateTicketStatus(id, status);
    loadTickets();
  };

  const deleteTicket = async (id: number) => {
    if (!confirm("Xóa ticket này?")) return;

    await ticketService.deleteTicket(id);
    loadTickets();
  };

  useEffect(() => {
    loadTickets();
  }, []);

  return (
    <main className="p-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">Tickets</h1>
        <a href="/tickets/new" className="bg-blue-600 text-white px-4 py-2 rounded">
          Add Ticket
        </a>
      </div>

      {error && <p className="text-red-600 mb-4">{error}</p>}

      <table className="w-full border bg-white">
        <thead>
          <tr className="bg-gray-100">
            <th className="border p-2">Title</th>
            <th className="border p-2">Description</th>
            <th className="border p-2">Priority</th>
            <th className="border p-2">Status</th>
            <th className="border p-2">Action</th>
          </tr>
        </thead>

        <tbody>
          {tickets.map((t) => (
            <tr key={t.id}>
              <td className="border p-2">{t.title}</td>
              <td className="border p-2">{t.description}</td>
              <td className="border p-2">{t.priority}</td>
              <td className="border p-2">{t.status}</td>
              <td className="border p-2 space-x-2">
                <button onClick={() => changeStatus(t.id, "PROCESSING")} className="text-blue-600 underline">
                  Processing
                </button>
                <button onClick={() => changeStatus(t.id, "RESOLVED")} className="text-green-600 underline">
                  Resolved
                </button>
                <button onClick={() => deleteTicket(t.id)} className="text-red-600 underline">
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