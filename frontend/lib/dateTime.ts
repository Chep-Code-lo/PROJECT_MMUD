type DateInput = string | number | Date | null | undefined;

const PROJECT_TIME_ZONE = "Asia/Ho_Chi_Minh";

const PROJECT_DATE_TIME_FORMATTER = new Intl.DateTimeFormat("vi-VN", {
  year: "numeric",
  month: "2-digit",
  day: "2-digit",
  hour: "2-digit",
  minute: "2-digit",
  second: "2-digit",
  hour12: false,
  timeZoneName: "short",
  timeZone: PROJECT_TIME_ZONE,
});

const UTC_DATE_TIME_FORMATTER = new Intl.DateTimeFormat("vi-VN", {
  year: "numeric",
  month: "2-digit",
  day: "2-digit",
  hour: "2-digit",
  minute: "2-digit",
  second: "2-digit",
  hour12: false,
  timeZoneName: "short",
  timeZone: "UTC",
});

function toDate(value: DateInput) {
  if (!value) {
    return null;
  }

  const date = value instanceof Date ? value : new Date(value);

  if (Number.isNaN(date.getTime())) {
    return null;
  }

  return date;
}

export function formatLocalDateTime(value: DateInput) {
  const date = toDate(value);

  if (!date) {
    return value ? String(value) : "-";
  }

  return PROJECT_DATE_TIME_FORMATTER.format(date);
}

export function formatUtcDateTime(value: DateInput) {
  const date = toDate(value);

  if (!date) {
    return value ? String(value) : "-";
  }

  return UTC_DATE_TIME_FORMATTER.format(date);
}

export function formatRelativeTime(value: DateInput, now: number = Date.now()) {
  const date = toDate(value);

  if (!date) {
    return "-";
  }

  const diffMs = now - date.getTime();
  const diffSeconds = Math.max(0, Math.floor(diffMs / 1000));

  if (diffSeconds < 5) {
    return "vua xong";
  }

  if (diffSeconds < 60) {
    return `${diffSeconds} giay truoc`;
  }

  const diffMinutes = Math.floor(diffSeconds / 60);
  if (diffMinutes < 60) {
    return `${diffMinutes} phut truoc`;
  }

  const diffHours = Math.floor(diffMinutes / 60);
  if (diffHours < 24) {
    return `${diffHours} gio truoc`;
  }

  const diffDays = Math.floor(diffHours / 24);
  return `${diffDays} ngay truoc`;
}
