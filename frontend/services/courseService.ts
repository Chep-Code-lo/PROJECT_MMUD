import axiosClient from "@/lib/axiosClient";
import type {
  CheckoutResponse,
  CourseDetail,
  CourseSummary,
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

  checkout: async (courseId: number) => {
    const res = await axiosClient.post<CheckoutResponse>(
      `/api/courses/${courseId}/checkout`
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
