import axiosClient from "@/lib/axiosClient";
import type { Ticket, TicketRequest, TicketStatus } from "@/types/ticket";

export const ticketService = {
  getTickets: async () => {
    const res = await axiosClient.get<Ticket[]>("/api/tickets");
    return res.data;
  },

  createTicket: async (data: TicketRequest) => {
    const res = await axiosClient.post<Ticket>("/api/tickets", data);
    return res.data;
  },

  updateTicketStatus: async (id: number, status: TicketStatus) => {
    const res = await axiosClient.put<Ticket>(`/api/tickets/${id}/status`, {
      status,
    });
    return res.data;
  },

  deleteTicket: async (id: number) => {
    await axiosClient.delete(`/api/tickets/${id}`);
  },
};