export type AuditLog = {
  id: number;
  action: string;
  entityType?: string | null;
  entityId?: number | null;
  actorUserId?: number | null;
  actorEmail?: string | null;
  success: boolean;
  details?: string | null;
  createdAt?: string;
};
