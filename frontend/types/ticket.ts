export type TicketStatus = "OPEN" | "PROCESSING" | "RESOLVED";

export type Ticket = {
  id: number;
  title: string;
  description: string;
  status: TicketStatus;
  priority: "LOW" | "MEDIUM" | "HIGH";
};

