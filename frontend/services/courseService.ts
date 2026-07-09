import axiosClient from "@/lib/axiosClient";
import type {
  CourseDetail,
  CourseSummary,
  EnrollmentRequestResponse,
  LessonDetail,
} from "@/types/course";

export const courseService = {
  getCourses: async () => {
    const res = await axiosClient.get<CourseSummary[]>("/api/courses");
    return res.data;
  },

  getCourse: async (courseId: number) => {
    const res = await axiosClient.get<CourseDetail>(`/api/courses/${courseId}`);
    return res.data;
  },

  requestEnrollment: async (courseId: number) => {
    const res = await axiosClient.post<EnrollmentRequestResponse>(
      `/api/courses/${courseId}/enrollment-requests`
    );
    return res.data;
  },

  getLesson: async (courseId: number, lessonId: number) => {
    const res = await axiosClient.get<LessonDetail>(
      `/api/courses/${courseId}/lessons/${lessonId}`
    );
    return res.data;
  },
};
