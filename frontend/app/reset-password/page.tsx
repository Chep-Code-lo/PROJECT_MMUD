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
      setError("Mat khau nhap lai khong khop.");
      return;
    }

    if (!token.trim()) {
      setLoading(false);
      setError("Vui long nhap ma xac nhan hop le.");
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

      setSuccess(response.message);
      setTimeout(() => router.push("/login"), 1200);
    } catch (err) {
      setError(getApiErrorMessage(err, "Khong the dat lai mat khau."));
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="mx-auto max-w-3xl">
      <section className="rounded-[34px] border border-[#1f2a24]/10 bg-white/86 p-8 shadow-[0_26px_60px_rgba(31,42,36,0.08)] md:p-10">
        <p className="mb-3 text-xs font-semibold uppercase tracking-[0.32em] text-[#8b5e34]">
          Mat khau moi
        </p>
        <h1 className="mb-3 text-3xl font-bold text-[#12372f]">
          Tao lai mat khau cho tai khoan
        </h1>
        <p className="mb-8 max-w-2xl text-sm leading-7 text-[#546159]">
          Nhap mat khau moi de hoan tat qua trinh khoi phuc tai khoan.
        </p>

        <form onSubmit={handleSubmit} className="space-y-5" autoComplete="off">
          {!token && (
            <Input
              label="Ma xac nhan"
              value={token}
              onChange={(event) => setToken(event.target.value)}
              autoComplete="off"
              required
            />
          )}

          <Input
            label="Mat khau moi"
            type="password"
            value={newPassword}
            onChange={(event) => setNewPassword(event.target.value)}
            autoComplete="new-password"
            required
          />

          <Input
            label="Nhap lai mat khau moi"
            type="password"
            value={confirmPassword}
            onChange={(event) => setConfirmPassword(event.target.value)}
            autoComplete="new-password"
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
              {loading ? "Dang xu ly..." : "Cap nhat mat khau"}
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
