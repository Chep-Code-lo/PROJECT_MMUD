import axiosClient from "@/lib/axiosClient";
import type { Customer, CustomerRequest } from "@/types/customer";

export const customerService = {
  getCustomers: async () => {
    const res = await axiosClient.get<Customer[]>("/api/customers");
    return res.data;
  },

  getCustomerById: async (id: number) => {
    const res = await axiosClient.get<Customer>(`/api/customers/${id}`);
    return res.data;
  },

  createCustomer: async (data: CustomerRequest) => {
    const res = await axiosClient.post<Customer>("/api/customers", data);
    return res.data;
  },

  updateCustomer: async (id: number, data: CustomerRequest) => {
    const res = await axiosClient.put<Customer>(`/api/customers/${id}`, data);
    return res.data;
  },

  deleteCustomer: async (id: number) => {
    await axiosClient.delete(`/api/customers/${id}`);
  },
};