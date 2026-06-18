export type TicketStatus = "OPEN" | "PROCESSING" | "RESOLVED";
export type TicketPriority = "LOW" | "MEDIUM" | "HIGH";

export type Ticket = {
  id: number;
  title: string;
  description: string;
  status: TicketStatus;
  priority: TicketPriority;
  createdById?: number;
  assignedToId?: number | null;
  createdBy?: string | null;
  assignedTo?: string | null;
  createdAt?: string;
  updatedAt?: string;
};

export type TicketRequest = {
  title: string;
  description: string;
  priority: TicketPriority;
  status?: TicketStatus;
  createdById: number;
  assignedToId?: number | null;
};
