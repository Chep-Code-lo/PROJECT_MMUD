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

      setSuccess(response.message);
    } catch (err) {
      setError(getApiErrorMessage(err, "Khong the gui yeu cau luc nay."));
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="mx-auto max-w-3xl">
      <section className="rounded-[34px] border border-[#1f2a24]/10 bg-white/86 p-8 shadow-[0_26px_60px_rgba(31,42,36,0.08)] md:p-10">
        <p className="mb-3 text-xs font-semibold uppercase tracking-[0.32em] text-[#8b5e34]">
          Dat lai mat khau
        </p>
        <h1 className="mb-3 text-3xl font-bold text-[#12372f]">
          Tim lai quyen truy cap tai khoan
        </h1>
        <p className="mb-8 max-w-2xl text-sm leading-7 text-[#546159]">
          Nhap email da dang ky. Neu thong tin hop le, he thong se huong dan ban tao
          mat khau moi.
        </p>

        <form onSubmit={handleSubmit} className="space-y-5" autoComplete="off">
          <Input
            label="Email"
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            autoComplete="off"
            required
          />

          {error && (
            <div className="rounded-2xl border border-[#b45309]/15 bg-[#fff4ea] px-4 py-3 text-sm text-[#9a3412]">
              {error}
            </div>
          )}

          {success && (
            <div className="rounded-2xl border border-[#0f766e]/15 bg-[#eef7f4] px-4 py-3 text-sm text-[#115e59]">
              {success}
            </div>
          )}

          <div className="flex flex-wrap items-center gap-3">
            <Button type="submit" disabled={loading}>
              {loading ? "Dang xu ly..." : "Tiep tuc"}
            </Button>
            <Link href="/login" className="text-sm font-semibold text-[#0f766e]">
              Quay lai dang nhap
            </Link>
          </div>
        </form>
      </section>
    </main>
  );
}
