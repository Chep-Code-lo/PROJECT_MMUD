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
      setError(getApiErrorMessage(err, "Dang ky that bai."));
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="mx-auto max-w-3xl">
      <section className="rounded-[34px] border border-[#1f2a24]/10 bg-white/86 p-8 shadow-[0_26px_60px_rgba(31,42,36,0.08)] md:p-10">
        <p className="mb-3 text-xs font-semibold uppercase tracking-[0.32em] text-[#8b5e34]">
          Student Registration
        </p>
        <h1 className="mb-3 text-3xl font-bold text-[#12372f]">
          Tao tai khoan de test JWT, bcrypt va AES-GCM
        </h1>
        <p className="mb-8 max-w-2xl text-sm leading-7 text-[#546159]">
          Tai khoan dang ky moi mac dinh la STUDENT. Phone number va billing address
          duoc backend ma hoa truoc khi luu vao database.
        </p>

        <form onSubmit={handleRegister} className="grid gap-5 md:grid-cols-2">
          <Input
            label="Full Name"
            value={form.fullName}
            onChange={(event) =>
              setForm((current) => ({ ...current, fullName: event.target.value }))
            }
            required
          />
          <Input
            label="Email"
            type="email"
            value={form.email}
            onChange={(event) =>
              setForm((current) => ({ ...current, email: event.target.value }))
            }
            required
          />
          <Input
            label="Password"
            type="password"
            value={form.password}
            onChange={(event) =>
              setForm((current) => ({ ...current, password: event.target.value }))
            }
            required
          />
          <Input
            label="Phone Number"
            value={form.phoneNumber}
            onChange={(event) =>
              setForm((current) => ({ ...current, phoneNumber: event.target.value }))
            }
          />
          <div className="md:col-span-2">
            <Input
              label="Billing Address"
              value={form.billingAddress}
              onChange={(event) =>
                setForm((current) => ({
                  ...current,
                  billingAddress: event.target.value,
                }))
              }
            />
          </div>

          {error && (
            <div className="md:col-span-2 rounded-2xl border border-[#b45309]/15 bg-[#fff4ea] px-4 py-3 text-sm text-[#9a3412]">
              {error}
            </div>
          )}

          <div className="md:col-span-2 flex flex-wrap items-center gap-3">
            <Button type="submit" disabled={loading}>
              {loading ? "Dang xu ly..." : "Dang ky tai khoan"}
            </Button>
            <Link href="/login" className="text-sm font-semibold text-[#0f766e]">
              Da co tai khoan? Dang nhap
            </Link>
          </div>
        </form>
      </section>
    </main>
  );
}
