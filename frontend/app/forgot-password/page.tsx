"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import Button from "@/components/Button";
import Input from "@/components/Input";
import { getApiErrorMessage } from "@/lib/apiError";
import { authService } from "@/services/authService";

const RESET_TOKEN_STORAGE_KEY = "passwordResetToken";

export default function ForgotPasswordPage() {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setLoading(true);
    setError("");
    setSuccess("");

    try {
      const response = await authService.requestPasswordReset({ email });

      if (typeof window !== "undefined" && response.demoResetToken) {
        window.sessionStorage.setItem(
          RESET_TOKEN_STORAGE_KEY,
          response.demoResetToken
        );
        router.push("/reset-password");
        return;
      }

      void response;
      setSuccess("Yêu cầu đặt lại mật khẩu đã được gửi thành công.");
    } catch (err) {
      setError(getApiErrorMessage(err, "Không thể gửi yêu cầu lúc này. Vui lòng thử lại sau."));
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="relative mx-auto max-w-3xl overflow-hidden py-6">
      <div className="pointer-events-none absolute -left-8 bottom-8 h-32 w-32 rounded-full bg-[#b45309]/12 blur-2xl animate-pulse-glow" />
      <section className="relative rounded-[30px] border border-[#1f2a24]/10 bg-white/90 p-7 shadow-[0_28px_70px_rgba(31,42,36,0.1)] backdrop-blur animate-fade-in-up md:p-10">
        <p className="mb-3 text-base font-bold text-[#8b5e34]">
          Đặt lại mật khẩu
        </p>
        <h1 className="mb-4 text-4xl font-bold leading-tight text-[#12372f] md:text-5xl">
          Tìm lại quyền truy cập tài khoản
        </h1>
        <p className="mb-8 max-w-2xl text-xl leading-8 text-[#546159]">
          Nhập email đã đăng ký để tiếp tục đặt lại mật khẩu.
        </p>

        <form onSubmit={handleSubmit} className="space-y-5" autoComplete="off">
          <Input
            label="Email"
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            autoComplete="off"
            placeholder="Nhập email của bạn"
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
              {loading ? "Đang xử lý..." : "Tiếp tục"}
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
