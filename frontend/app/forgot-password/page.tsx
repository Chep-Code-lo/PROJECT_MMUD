"use client";

import { useState } from "react";

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState("");
  const [message, setMessage] = useState("");

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    setMessage(
      "Nếu email tồn tại trong hệ thống, hướng dẫn đặt lại mật khẩu sẽ được gửi đến email của bạn."
    );
  };

  return (
    <main className="min-h-screen flex items-center justify-center bg-gray-100">
      <form
        onSubmit={handleSubmit}
        className="bg-white p-8 rounded-xl shadow-md w-full max-w-md text-gray-900"
      >
        <h1 className="text-2xl font-bold mb-4 text-center text-gray-900">
          Quên mật khẩu
        </h1>

        <p className="text-sm text-gray-700 mb-6 text-center">
          Nhập email tài khoản của bạn để nhận hướng dẫn đặt lại mật khẩu.
        </p>

        {message && <p className="mb-4 text-green-600 text-sm">{message}</p>}

        <label className="block mb-2 text-gray-900">Email</label>
        <input
          className="w-full border p-2 rounded mb-6 text-gray-900 bg-white"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          type="email"
          required
        />

        <button className="w-full bg-blue-600 text-white py-2 rounded hover:bg-blue-700">
          Gửi yêu cầu
        </button>

        <p className="text-sm text-center mt-4 text-gray-900">
          Nhớ mật khẩu rồi?{" "}
          <a href="/login" className="text-blue-600 underline">
            Quay lại đăng nhập
          </a>
        </p>
      </form>
    </main>
  );
}