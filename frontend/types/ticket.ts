export type TicketStatus = "OPEN" | "PROCESSING" | "RESOLVED";

export type Ticket = {
  id: number;
  title: string;
  description: string;
  status: TicketStatus;
  priority: string;
  createdBy?: string;
  assignedTo?: string;
  createdAt?: string;
  updatedAt?: string;
};

export type TicketRequest = {
  title: string;
  description: string;
  priority: string;
};