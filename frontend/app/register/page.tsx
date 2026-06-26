"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import Button from "@/components/Button";
import Input from "@/components/Input";
import { getApiErrorMessage } from "@/lib/apiError";
import { authService } from "@/services/authService";

export default function RegisterPage() {
  const router = useRouter();
  const [form, setForm] = useState({
    fullName: "",
    email: "",
    password: "",
    phoneNumber: "",
    billingAddress: "",
  });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleRegister = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setLoading(true);
    setError("");

    try {
      await authService.register(form);
      router.push("/courses");
    } catch (err) {
      setError(getApiErrorMessage(err, "Đăng ký thất bại. Vui lòng kiểm tra lại thông tin đã nhập."));
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="relative mx-auto max-w-4xl overflow-hidden py-6">
      <div className="pointer-events-none absolute -right-8 top-8 h-32 w-32 rounded-full bg-[#0f766e]/12 blur-2xl animate-pulse-glow" />
      <section className="relative rounded-[30px] border border-[#1f2a24]/10 bg-white/90 p-7 shadow-[0_28px_70px_rgba(31,42,36,0.1)] backdrop-blur animate-fade-in-up md:p-10">
        <p className="mb-3 text-base font-bold text-[#8b5e34]">
          Đăng ký tài khoản
        </p>
        <h1 className="mb-4 text-4xl font-bold leading-tight text-[#12372f] md:text-5xl">
          Tạo tài khoản học viên mới
        </h1>

        <form onSubmit={handleRegister} className="grid gap-5 md:grid-cols-2">
          <Input
            label="Họ và tên"
            value={form.fullName}
            onChange={(event) =>
              setForm((current) => ({ ...current, fullName: event.target.value }))
            }
            placeholder="Nhập họ và tên"
            required
          />
          <Input
            label="Email"
            type="email"
            value={form.email}
            onChange={(event) =>
              setForm((current) => ({ ...current, email: event.target.value }))
            }
            placeholder="Nhập email của bạn"
            required
          />
          <Input
            label="Mật khẩu"
            type="password"
            value={form.password}
            onChange={(event) =>
              setForm((current) => ({ ...current, password: event.target.value }))
            }
            autoComplete="new-password"
            placeholder="Tạo mật khẩu"
            required
          />
          <Input
            label="Số điện thoại"
            value={form.phoneNumber}
            onChange={(event) =>
              setForm((current) => ({ ...current, phoneNumber: event.target.value }))
            }
            placeholder="Nhập số điện thoại"
          />
          <div className="md:col-span-2">
            <Input
              label="Địa chỉ"
              value={form.billingAddress}
              onChange={(event) =>
                setForm((current) => ({
                  ...current,
                  billingAddress: event.target.value,
                }))
              }
              placeholder="Nhập địa chỉ"
            />
          </div>

          {error && (
            <div className="rounded-2xl border border-[#b45309]/18 bg-[#fff4ea] px-5 py-4 text-base font-semibold text-[#9a3412] md:col-span-2">
              {error}
            </div>
          )}

          <div className="md:col-span-2 flex flex-wrap items-center gap-3">
            <Button type="submit" disabled={loading}>
              {loading ? "Đang xử lý..." : "Đăng ký tài khoản"}
            </Button>
            <Link href="/login" className="text-lg font-bold text-[#8b5e34] transition hover:text-[#6f4725]">
              Đã có tài khoản? Đăng nhập
            </Link>
          </div>
        </form>
      </section>
    </main>
  );
}
