"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import Button from "@/components/Button";
import Input from "@/components/Input";
import { getApiErrorMessage } from "@/lib/apiError";
import { authService } from "@/services/authService";

export default function LoginPage() {
  const router = useRouter();
  const [form, setForm] = useState({
    email: "",
    password: "",
  });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleLogin = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setLoading(true);
    setError("");

    try {
      await authService.login(form);
      router.push("/courses");
    } catch (err) {
      setError(getApiErrorMessage(err, "Dang nhap that bai."));
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="mx-auto max-w-4xl">
      <div className="grid gap-6 md:grid-cols-[0.95fr_1.05fr]">
        <section className="rounded-[32px] border border-[#1f2a24]/10 bg-[#12372f] p-8 text-[#f7f5ef] shadow-[0_24px_60px_rgba(18,55,47,0.24)]">
          <p className="mb-3 text-xs font-semibold uppercase tracking-[0.3em] text-[#cddbd2]">
            Chao mung quay tro lai
          </p>
          <h1 className="mb-4 text-3xl font-bold">Dang nhap tai khoan hoc vien</h1>
          <p className="mb-6 text-sm leading-7 text-[#dbe4de]">
            Dang nhap de tiep tuc hoc tap, xem khoa hoc da dang ky va theo doi ket qua
            cua ban tren he thong.
          </p>
          <div className="rounded-2xl border border-white/15 bg-white/8 px-4 py-4 text-sm leading-7 text-[#dbe4de]">
            Neu ban quen mat khau, co the dat lai trong vai buoc ngay tren ung dung.
          </div>
        </section>

        <section className="rounded-[32px] border border-[#1f2a24]/10 bg-white/86 p-8 shadow-[0_24px_60px_rgba(31,42,36,0.08)]">
          <form onSubmit={handleLogin} className="space-y-5" autoComplete="off">
            <Input
              label="Email"
              type="email"
              value={form.email}
              onChange={(event) =>
                setForm((current) => ({ ...current, email: event.target.value }))
              }
              placeholder="student1@example.com"
              autoComplete="off"
              required
            />

            <Input
              label="Password"
              type="password"
              value={form.password}
              onChange={(event) =>
                setForm((current) => ({ ...current, password: event.target.value }))
              }
              autoComplete="off"
              required
            />

            {error && (
              <div className="rounded-2xl border border-[#b45309]/15 bg-[#fff4ea] px-4 py-3 text-sm text-[#9a3412]">
                {error}
              </div>
            )}

            <Button type="submit" className="w-full" disabled={loading}>
              {loading ? "Dang xu ly..." : "Dang nhap"}
            </Button>
          </form>

          <div className="mt-6 flex flex-wrap items-center gap-4 text-sm text-[#526059]">
            <Link href="/forgot-password" className="font-semibold text-[#0f766e]">
              Quen mat khau?
            </Link>
            <span className="text-[#9aa29d]">/</span>
            <p>
              Chua co tai khoan?{" "}
              <Link href="/register" className="font-semibold text-[#0f766e]">
                Dang ky ngay
              </Link>
            </p>
          </div>
        </section>
      </div>
    </main>
  );
}
