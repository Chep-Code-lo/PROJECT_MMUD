"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import Button from "@/components/Button";
import Input from "@/components/Input";
import { getApiErrorMessage } from "@/lib/apiError";
import { authService } from "@/services/authService";

const RESET_TOKEN_STORAGE_KEY = "passwordResetToken";

export default function ResetPasswordPage() {
  const router = useRouter();
  const [token, setToken] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (typeof window !== "undefined") {
      const tokenFromQuery = new URLSearchParams(window.location.search).get("token");
      if (tokenFromQuery) {
        setToken(tokenFromQuery);
        return;
      }

      const storedToken = window.sessionStorage.getItem(RESET_TOKEN_STORAGE_KEY);
      if (storedToken) {
        setToken(storedToken);
      }
    }
  }, []);

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setLoading(true);
    setError("");
    setSuccess("");

    if (newPassword !== confirmPassword) {
      setLoading(false);
      setError("Mật khẩu nhập lại không khớp.");
      return;
    }

    if (!token.trim()) {
      setLoading(false);
      setError("Vui lòng nhập mã xác nhận hợp lệ.");
      return;
    }

    try {
      const response = await authService.resetPassword({
        token: token.trim(),
        newPassword,
      });

      if (typeof window !== "undefined") {
        window.sessionStorage.removeItem(RESET_TOKEN_STORAGE_KEY);
      }

      void response;
      setSuccess("Mật khẩu đã được cập nhật thành công.");
      setTimeout(() => router.push("/login"), 1200);
    } catch (err) {
      setError(getApiErrorMessage(err, "Không thể đặt lại mật khẩu. Vui lòng thử lại sau."));
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="relative mx-auto max-w-3xl overflow-hidden py-6">
      <div className="pointer-events-none absolute -right-8 top-8 h-32 w-32 rounded-full bg-[#0f766e]/12 blur-2xl animate-pulse-glow" />
      <section className="relative rounded-[30px] border border-[#1f2a24]/10 bg-white/90 p-7 shadow-[0_28px_70px_rgba(31,42,36,0.1)] backdrop-blur animate-fade-in-up md:p-10">
        <p className="mb-3 text-base font-bold text-[#8b5e34]">
          Mật khẩu mới
        </p>
        <h1 className="mb-4 text-4xl font-bold leading-tight text-[#12372f] md:text-5xl">
          Tạo lại mật khẩu cho tài khoản
        </h1>
        <p className="mb-8 max-w-2xl text-xl leading-8 text-[#546159]">
          Nhập mật khẩu mới để hoàn tất quá trình khôi phục tài khoản.
        </p>

        <form onSubmit={handleSubmit} className="space-y-5" autoComplete="off">
          {!token && (
            <Input
              label="Mã xác nhận"
              value={token}
              onChange={(event) => setToken(event.target.value)}
              autoComplete="off"
              placeholder="Nhập mã xác nhận"
              required
            />
          )}

          <Input
            label="Mật khẩu mới"
            type="password"
            value={newPassword}
            onChange={(event) => setNewPassword(event.target.value)}
            autoComplete="new-password"
            placeholder="Nhập mật khẩu mới"
            required
          />

          <Input
            label="Nhập lại mật khẩu mới"
            type="password"
            value={confirmPassword}
            onChange={(event) => setConfirmPassword(event.target.value)}
            autoComplete="new-password"
            placeholder="Nhập lại mật khẩu mới"
            required
          />

          {error && (
            <div className="rounded-2xl border border-[#b45309]/18 bg-[#fff4ea] px-5 py-4 text-base font-semibold text-[#9a3412]">
              {error}
            </div>
          )}

          {success && (
            <div className="rounded-2xl border border-[#0f766e]/18 bg-[#eef7f4] px-5 py-4 text-base font-semibold text-[#115e59]">
              {success}
            </div>
          )}

          <div className="flex flex-wrap items-center gap-3">
            <Button type="submit" disabled={loading}>
              {loading ? "Đang xử lý..." : "Cập nhật mật khẩu"}
            </Button>
            <Link href="/login" className="text-lg font-bold text-[#8b5e34] transition hover:text-[#6f4725]">
              Quay lại đăng nhập
            </Link>
          </div>
        </form>
      </section>
    </main>
  );
}
