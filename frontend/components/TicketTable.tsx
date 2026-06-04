import type { Ticket, TicketStatus } from "@/types/ticket";
import Button from "./Button";

type TicketTableProps = {
  tickets: Ticket[];
  onDelete?: (id: number) => void;
  onChangeStatus?: (id: number, status: TicketStatus) => void;
};

export default function TicketTable({
  tickets,
  onDelete,
  onChangeStatus,
}: TicketTableProps) {
  if (tickets.length === 0) {
    return (
      <div className="bg-white p-6 rounded-xl shadow text-gray-900">
        Chưa có ticket nào.
      </div>
    );
  }

  return (
    <div className="overflow-x-auto bg-white rounded-xl shadow">
      <table className="w-full border-collapse text-gray-900">
        <thead>
          <tr className="bg-gray-100 text-left">
            <th className="border p-3">ID</th>
            <th className="border p-3">Title</th>
            <th className="border p-3">Description</th>
            <th className="border p-3">Priority</th>
            <th className="border p-3">Status</th>
            <th className="border p-3">Action</th>
          </tr>
        </thead>

        <tbody>
          {tickets.map((ticket) => (
            <tr key={ticket.id} className="hover:bg-gray-50">
              <td className="border p-3">{ticket.id}</td>
              <td className="border p-3">{ticket.title}</td>
              <td className="border p-3">{ticket.description}</td>
              <td className="border p-3">{ticket.priority}</td>
              <td className="border p-3">
                <span className="px-2 py-1 rounded bg-blue-100 text-blue-700 text-sm">
                  {ticket.status}
                </span>
              </td>
              <td className="border p-3">
                <div className="flex flex-wrap gap-2">
                  {onChangeStatus && (
                    <>
                      <Button
                        variant="secondary"
                        onClick={() =>
                          onChangeStatus(ticket.id, "PROCESSING")
                        }
                      >
                        Processing
                      </Button>

                      <Button
                        variant="success"
                        onClick={() =>
                          onChangeStatus(ticket.id, "RESOLVED")
                        }
                      >
                        Resolved
                      </Button>
                    </>
                  )}

                  {onDelete && (
                    <Button
                      variant="danger"
                      onClick={() => onDelete(ticket.id)}
                    >
                      Delete
                    </Button>
                  )}
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}