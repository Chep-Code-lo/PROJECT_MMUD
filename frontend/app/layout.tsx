import type { Metadata } from "next";
import Navbar from "@/components/Navbar";
import "./globals.css";

export const metadata: Metadata = {
  title: "Hệ thống khóa học bảo mật ứng dụng mạng",
  description: "Mô hình khóa học bảo mật ứng dụng mạng dựa trên nền tảng đám mây.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="vi">
      <body>
        <Navbar />
        <div className="mx-auto max-w-6xl px-6 py-8 md:px-10">{children}</div>
      </body>
    </html>
  );
}
