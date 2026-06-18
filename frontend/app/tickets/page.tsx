"use client";

import { useEffect, useState } from "react";
import Navbar from "@/components/Navbar";
import ProtectedRoute from "@/components/ProtectedRoute";
import TicketTable from "@/components/TicketTable";
import { ticketService } from "@/services/ticketService";
import type { Ticket, TicketStatus } from "@/types/ticket";

export default function TicketsPage() {
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [error, setError] = useState("");

  const loadTickets = async () => {
    try {
      const data = await ticketService.getTickets();
      setTickets(data);
      setError("");
    } catch {
      setError("Khong tai duoc ticket. Co the ban chua dang nhap hoac khong du quyen.");
    }
  };

  const handleChangeStatus = async (id: number, status: TicketStatus) => {
    try {
      await ticketService.updateTicketStatus(id, status);
      await loadTickets();
    } catch {
      setError("Khong cap nhat duoc trang thai ticket.");
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm("Xoa ticket nay?")) return;

    try {
      await ticketService.deleteTicket(id);
      await loadTickets();
    } catch {
      setError("Khong xoa duoc ticket.");
    }
  };

  useEffect(() => {
    loadTickets();
  }, []);

  return (
    <ProtectedRoute allowedRoles={["ADMIN", "STAFF", "USER"]}>
      <main className="min-h-screen bg-gray-100 text-gray-900">
        <Navbar />

        <section className="max-w-5xl mx-auto p-8">
          <div className="flex justify-between items-center mb-6 gap-4">
            <div>
              <h1 className="text-3xl font-bold">Ticket Management</h1>
              <p className="text-gray-700 mt-2">
                Luong toi thieu phai co tao ticket, cap nhat trang thai va phan
                biet ro quyen truy cap giua ADMIN, STAFF va USER.
              </p>
            </div>

            <a
              href="/tickets/new"
              className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
            >
              Add Ticket
            </a>
          </div>

          {error && <p className="text-red-600 mb-4">{error}</p>}

          <TicketTable
            tickets={tickets}
            onDelete={handleDelete}
            onChangeStatus={handleChangeStatus}
          />
        </section>
      </main>
    </ProtectedRoute>
  );
}
