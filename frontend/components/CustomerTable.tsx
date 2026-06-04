import type { Customer } from "@/types/customer";
import Button from "./Button";

type CustomerTableProps = {
  customers: Customer[];
  onDelete?: (id: number) => void;
};

export default function CustomerTable({
  customers,
  onDelete,
}: CustomerTableProps) {
  if (customers.length === 0) {
    return (
      <div className="bg-white p-6 rounded-xl shadow text-gray-900">
        Chưa có khách hàng nào.
      </div>
    );
  }

  return (
    <div className="overflow-x-auto bg-white rounded-xl shadow">
      <table className="w-full border-collapse text-gray-900">
        <thead>
          <tr className="bg-gray-100 text-left">
            <th className="border p-3">ID</th>
            <th className="border p-3">Name</th>
            <th className="border p-3">Email</th>
            <th className="border p-3">Phone</th>
            <th className="border p-3">Address</th>
            <th className="border p-3">Tax Code</th>
            <th className="border p-3">Action</th>
          </tr>
        </thead>

        <tbody>
          {customers.map((customer) => (
            <tr key={customer.id} className="hover:bg-gray-50">
              <td className="border p-3">{customer.id}</td>
              <td className="border p-3">{customer.name}</td>
              <td className="border p-3">{customer.email}</td>
              <td className="border p-3">{customer.phone}</td>
              <td className="border p-3">{customer.address}</td>
              <td className="border p-3">{customer.taxCode}</td>
              <td className="border p-3">
                {onDelete && (
                  <Button
                    variant="danger"
                    onClick={() => onDelete(customer.id)}
                  >
                    Delete
                  </Button>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}