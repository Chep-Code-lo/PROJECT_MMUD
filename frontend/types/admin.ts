import type { UserProfile } from "@/types/auth";

export type AdminEnrollment = {
  enrollmentId: number;
  studentId: number;
  studentName: string;
  studentEmail: string;
  courseId: number;
  courseTitle: string;
  status: "PENDING" | "ACTIVE" | "CANCELLED";
  createdAt?: string;
  activatedAt?: string | null;
  certificateIssued: boolean;
};

export type AdminCourseOverview = {
  id: number;
  title: string;
  summary: string;
  price: number;
  instructorName: string;
  activeStudentCount: number;
  pendingRequestCount: number;
};

export type AdminCourseRoster = {
  courseId: number;
  courseTitle: string;
  courseSummary: string;
  price: number;
  instructorName: string;
  activeStudentCount: number;
  pendingRequestCount: number;
  activeStudents: AdminEnrollment[];
  pendingRequests: AdminEnrollment[];
};

export type AdminStudentProfile = UserProfile;
