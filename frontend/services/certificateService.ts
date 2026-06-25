import axiosClient from "@/lib/axiosClient";
import type { Certificate } from "@/types/course";

export const certificateService = {
  getMine: async () => {
    const res = await axiosClient.get<Certificate[]>("/api/certificates/me");
    return res.data;
  },

  getById: async (certificateId: number) => {
    const res = await axiosClient.get<Certificate>(
      `/api/certificates/${certificateId}`
    );
    return res.data;
  },
};
