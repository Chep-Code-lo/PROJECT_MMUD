import axiosClient from "@/lib/axiosClient";
import type {
  AdminCourseOverview,
  AdminCourseRoster,
  AdminEnrollment,
  AdminStudentProfile,
} from "@/types/admin";

export const adminService = {
  getCourses: async () => {
    const res = await axiosClient.get<AdminCourseOverview[]>("/api/admin/courses");
    return res.data;
  },

  getCourseRoster: async (courseId: number) => {
    const res = await axiosClient.get<AdminCourseRoster>(
      `/api/admin/courses/${courseId}/enrollments`
    );
    return res.data;
  },

  approveEnrollment: async (enrollmentId: number) => {
    const res = await axiosClient.post<AdminEnrollment>(
      `/api/admin/enrollments/${enrollmentId}/approve`
    );
    return res.data;
  },

  removeEnrollment: async (enrollmentId: number) => {
    await axiosClient.delete(`/api/admin/enrollments/${enrollmentId}`);
  },

  addStudentToCourse: async (courseId: number, email: string) => {
    const res = await axiosClient.post<AdminEnrollment>(
      `/api/admin/courses/${courseId}/students`,
      { email }
    );
    return res.data;
  },

  getStudentProfile: async (userId: number) => {
    const res = await axiosClient.get<AdminStudentProfile>(
      `/api/admin/users/${userId}`
    );
    return res.data;
  },
};
