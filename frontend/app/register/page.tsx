"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { authService } from "@/services/authService";

export default function RegisterPage() {
  const router = useRouter();

  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setSuccess("");

    try {
      await authService.register({ fullName, email, password });
      setSuccess("Đăng ký thành công. Đang chuyển sang đăng nhập...");
      setTimeout(() => router.push("/login"), 1000);
    } catch {
      setError("Đăng ký thất bại. Email có thể đã tồn tại.");
    }
  };

  return (
    <main className="min-h-screen flex items-center justify-center bg-gray-100">
      <form
        onSubmit={handleRegister}
        className="bg-white p-8 rounded-xl shadow-md w-full max-w-md text-gray-900"
      >
        <h1 className="text-2xl font-bold mb-6 text-center text-gray-900">
          Register
        </h1>

        {error && <p className="mb-4 text-red-600 text-sm">{error}</p>}
        {success && <p className="mb-4 text-green-600 text-sm">{success}</p>}

        <label className="block mb-2 text-gray-900">Full name</label>
        <input
          className="w-full border p-2 rounded mb-4 text-gray-900 bg-white"
          value={fullName}
          onChange={(e) => setFullName(e.target.value)}
          required
        />

        <label className="block mb-2 text-gray-900">Email</label>
        <input
          className="w-full border p-2 rounded mb-4 text-gray-900 bg-white"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          type="email"
          required
        />

        <label className="block mb-2 text-gray-900">Password</label>
        <input
          className="w-full border p-2 rounded mb-6 text-gray-900 bg-white"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          type="password"
          required
        />

        <button className="w-full bg-blue-600 text-white py-2 rounded hover:bg-blue-700">
          Đăng ký
        </button>

        <p className="text-sm text-center mt-4 text-gray-900">
          Đã có tài khoản?{" "}
          <a href="/login" className="text-blue-600 underline">
            Đăng nhập
          </a>
        </p>

        <p className="mt-4 text-xs text-gray-600 text-center">
          Tài khoản tự đăng ký mặc định mang role USER.
        </p>
      </form>
    </main>
  );
}
