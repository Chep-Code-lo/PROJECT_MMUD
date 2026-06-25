export type AuditLog = {
  id: number;
  actorUserId?: number | null;
  actorEmail?: string | null;
  action: string;
  targetType?: string | null;
  targetId?: number | null;
  ipAddress?: string | null;
  userAgent?: string | null;
  status: string;
  message: string;
  createdAt?: string;
};
