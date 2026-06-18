"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { authService } from "@/services/authService";

export default function LoginPage() {
  const router = useRouter();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");

    try {
      await authService.login({ email, password });
      router.push("/dashboard");
    } catch {
      setError("Đăng nhập thất bại. Kiểm tra email hoặc mật khẩu.");
    }
  };

  return (
    <main className="min-h-screen flex items-center justify-center bg-gray-100">
      <form
        onSubmit={handleLogin}
        className="bg-white p-8 rounded-xl shadow-md w-full max-w-md text-gray-900"
      >
        <h1 className="text-2xl font-bold mb-6 text-center text-gray-900">
          Login
        </h1>

        {error && <p className="mb-4 text-red-600 text-sm">{error}</p>}

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
          className="w-full border p-2 rounded mb-2 text-gray-900 bg-white"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          type="password"
          required
        />

        <button className="w-full bg-blue-600 text-white py-2 rounded hover:bg-blue-700 mt-6">
          Đăng nhập
        </button>

        <p className="text-sm text-center mt-4 text-gray-900">
          Chưa có tài khoản?{" "}
          <a href="/register" className="text-blue-600 underline">
            Đăng ký
          </a>
        </p>
      </form>
    </main>
  );
}
