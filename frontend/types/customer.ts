export type Customer = {
  id: number;
  name: string;
  email: string;
  phone: string;
  address: string;
  taxCode: string;
  createdAt?: string;
  updatedAt?: string;
};

export type CustomerRequest = {
  name: string;
  email: string;
  phone: string;
  address: string;
  taxCode: string;
};