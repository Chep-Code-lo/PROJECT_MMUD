export type CourseSummary = {
  id: number;
  title: string;
  summary: string;
  price: number;
  instructorName: string;
};

export type LessonPreview = {
  id: number;
  title: string;
  previewText: string;
  sortOrder: number;
  unlocked: boolean;
};

export type CourseDetail = {
  id: number;
  title: string;
  summary: string;
  description: string;
  price: number;
  instructorName: string;
  enrolled: boolean;
  lessons: LessonPreview[];
};

export type LessonDetail = {
  id: number;
  courseId: number;
  courseTitle: string;
  title: string;
  previewText: string;
  content?: string | null;
  sortOrder: number;
  unlocked: boolean;
};

export type EnrollmentRequestResponse = {
  enrollmentId: number;
  courseId: number;
  courseTitle: string;
  status: "PENDING" | "ACTIVE" | "CANCELLED";
  message: string;
};

export type Enrollment = {
  id: number;
  studentId: number;
  courseId: number;
  courseTitle: string;
  status: "PENDING" | "ACTIVE" | "CANCELLED";
  createdAt?: string;
  activatedAt?: string | null;
  certificateIssued: boolean;
};

export type Certificate = {
  id: number;
  ownerUserId: number;
  ownerEmail: string;
  courseId: number;
  courseTitle: string;
  score: number;
  certificateCode: string;
  issuedAt: string;
};
