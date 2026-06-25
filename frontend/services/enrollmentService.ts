import axiosClient from "@/lib/axiosClient";
import type { Enrollment } from "@/types/course";

export const enrollmentService = {
  getMine: async () => {
    const res = await axiosClient.get<Enrollment[]>("/api/enrollments/me");
    return res.data;
  },
};
