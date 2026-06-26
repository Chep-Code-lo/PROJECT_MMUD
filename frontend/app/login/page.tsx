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
      const response = await authService.login(form);
      router.push(response.user.role === "ADMIN" ? "/admin/courses" : "/courses");
    } catch (err) {
      setError(getApiErrorMessage(err, "Đăng nhập thất bại. Vui lòng kiểm tra lại email và mật khẩu."));
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="relative mx-auto max-w-5xl overflow-hidden py-6">
      <div className="pointer-events-none absolute -left-10 top-10 h-32 w-32 rounded-full bg-[#0f766e]/12 blur-2xl animate-pulse-glow" />
      <div className="pointer-events-none absolute -right-8 bottom-8 h-36 w-36 rounded-full bg-[#b45309]/12 blur-2xl animate-pulse-glow" />

      <div className="relative grid gap-6 md:grid-cols-[0.92fr_1.08fr] animate-fade-in-up">
        <section className="rounded-[30px] border border-white/12 bg-[#12372f] p-8 text-[#f7f5ef] shadow-[0_28px_70px_rgba(18,55,47,0.26)] md:p-10">
          <p className="mb-3 text-base font-bold text-[#d7e6dd]">
            Welcome
          </p>
          <h1 className="mb-5 text-4xl font-bold leading-tight md:text-5xl">
            Đăng nhập tài khoản hệ thống
          </h1>
          <p className="text-xl leading-8 text-[#dbe4de]">
            Truy cập tài khoản để tiếp tục học tập, quản lý khóa học và theo dõi thông tin cá nhân.
          </p>
        </section>

        <section className="rounded-[30px] border border-[#1f2a24]/10 bg-white/90 p-7 shadow-[0_26px_64px_rgba(31,42,36,0.1)] backdrop-blur md:p-10">
          <form onSubmit={handleLogin} className="space-y-5" autoComplete="off">
            <Input
              label="Email"
              type="email"
              value={form.email}
              onChange={(event) =>
                setForm((current) => ({ ...current, email: event.target.value }))
              }
              placeholder="Nhập email của bạn"
              autoComplete="off"
              required
            />

            <Input
              label="Mật khẩu"
              type="password"
              value={form.password}
              onChange={(event) =>
                setForm((current) => ({ ...current, password: event.target.value }))
              }
              autoComplete="off"
              placeholder="Nhập mật khẩu"
              required
            />

            {error && (
              <div className="rounded-2xl border border-[#b45309]/18 bg-[#fff4ea] px-5 py-4 text-base font-semibold text-[#9a3412]">
                {error}
              </div>
            )}

            <Button type="submit" className="w-full" disabled={loading}>
              {loading ? "Đang xử lý..." : "Đăng nhập"}
            </Button>
          </form>

          <div className="mt-6 flex flex-wrap items-center gap-4 text-lg text-[#526059]">
            <Link href="/forgot-password" className="font-bold text-[#0f766e] transition hover:text-[#115e59]">
              Quên mật khẩu?
            </Link>
            <span className="text-[#9aa29d]">/</span>
            <p>
              Chưa có tài khoản?{" "}
              <Link href="/register" className="font-bold text-[#8b5e34] transition hover:text-[#6f4725]">
                Đăng ký ngay
              </Link>
            </p>
          </div>
        </section>
      </div>
    </main>
  );
}
